package com.heymitch.skiplist;

// http://www.cs.bgu.ac.il/~mpam092/wiki.files/LazySkipList.pdf
// the paper this code is based on

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ConcurrentSkipList<T> {
    static final int MAX_LEVEL = 64; // Limits list height
    static int max_level_inserted = 0; // highest level we've placed a number
    final Node<T> head = new Node<T>(Integer.MIN_VALUE);
    final Node<T> tail = new Node<T>(Integer.MAX_VALUE);

    public ConcurrentSkipList() {
    	//System.out.println("creating skip list");
        for (int i = 0; i < head.next.length; i++) {
        	//System.out.println("making another node the tail (" + (i+1) + ")");
            head.next[i] = tail;
        }
    }

    // returns true if the value is in the list
    boolean contains(T x) {
        Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
        int lFound = find(x, preds, succs);
        return (lFound != -1
                && succs[lFound].fullyLinked	// fullyDeleted is true when a thread has finished inserting the node
                									// and setting up all of its predecessor/successor nodes at each lvl
                									// if it isn't fully linked it isn't fully inserted, or is being del'd
                && !succs[lFound].marked);		// marked represents if a node is being messed with (usually deleted)
    }

    // returns the index at which the node is 
    int find(T x, Node<T>[] preds, Node<T>[] succs) {
        int key = x.hashCode();	// key is hashCode of whatever x is
        int lFound = -1;
        Node<T> pred = head;	// start looking at the HEAD of the list
        for (int level = max_level_inserted; level >= 0; level--) {	// iterate over every LEVEL, starting w/ MAX_LEVEL
        	if(level > MAX_LEVEL) continue;			// fix case from my add-on that might cause us to look at invalid level
            Node<T> curr = pred.next[level];		// current node (starts at HEAD, progresses forward, like linklist)
            while (key > curr.key) {				// while the hashCode of element we want > hash of current node
            											// this works because elements are ordered by their hashCode
            											// this is numeric, like when we did hashcodes for hashtable
            											// in 367
                pred = curr;						// update pred(ecessor) pointer to curr
                curr = pred.next[level];			// update curr(ent) pointer to pred(ecessor).next at current level
                										// each node stores an array of nexts, one for each level!
                										// we can look at a node, and say, what's your next for [x] level
            }
            if (lFound == -1 && key == curr.key) {	// did we find T x?
                lFound = level;						// indicate we did by setting lFound (level found at)
            }
            preds[level] = pred;	// builds an array of nodes BEFORE the one we're looking for
            						// storing the predecessor for node we want for that level
            succs[level] = curr;	// builds an array of nodes AFTER the one we're looking for
									// storing the successor for node we want for that level
        }
        return lFound;				// returns the level the key was found at (-1 if not found)
    }

    boolean add(T x) {
        int topLevel = randomLevel();							// get a randomLevel to make the toplevel of new node
																// set top level we've inserted a node, prevents unnecessarily
																	// looking at empty levels during a find
        if( topLevel > max_level_inserted ) max_level_inserted = topLevel;
        Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];	// an array of nodes directly previous to the
        															// node we're adding at EACH LEVEL
        															// this varies, because not all nodes are at
        															// each level :D
        															// preds[level] = node before one we add at that lvl
        Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];	// an array of nodes directly after the node
        															// we're adding at EACH LEVEL
        															// succs[level] = node after one we add at that lvl
        
        while (true) {		// 	Think of this while loop as a spinlock, it will repeatedly see if conditions
        						// are right to add the node, and get it fully linked to predecessors, successors
        						// at every level of the skiplist
        					
            int lFound = find(x, preds, succs);				// check if the node is already in the list
            if (lFound != -1) {
                Node<T> nodeFound = succs[lFound];
                if (!nodeFound.marked) {					// marked = being removed currently, but not fully removed
                												// if it's marked we can skip it, it's dead to us
                    while (!nodeFound.fullyLinked) {    }	// the fullyLinked property is set on a node
                    											// when it has been fully inserted at all
                    											// proper levels in the skiplist and has had all
                    											// the proper predecessor / successor pointers set
                    										// if the fullyLinked property was false, let's pause
                    											// the adding process, checking over and over
                    											// for the other thread that is running to finish
                    											// linking the node this thread is looking at
                    return false;							// return false. Why?
                    											// we can't add this node, it's already in the skiplist!
                    											// we're here because we did the find() and found it
                    											// can't have doubles.
                }
                continue;
            }
            							// if we got this far, the skiplist DOESN'T already have the value in it
            int highestLocked = -1;	
            
            // HOKAY, SO, for inserting a node, what we have to do is determine what levels it's all
            // got to be thrown in to, as well as what predecessor and successor it should point to at
            // every level, right?
            
            // what we do is first generate the list of what the predecessors and successors should
            // be at every level at #1, see #1 below, and LOCK the predecessor
            // That predecessor needs to be locked so that we have a valid place to insert this new node
            // The successor doesn't need to be locked, because when we finally get around to inserting this,
            // we can just ask the predecessor for its .next, and make that the successor right away.
            
            try {
                Node<T> pred, succ;
                boolean valid = true;
                for (int level = 0; valid && (level <= topLevel); level++) { // #1 #1 #1 #1
                    pred = preds[level];
                    succ = succs[level];
                    pred.lock.lock();	// lock predecessor so we have a nice place to insert
                    highestLocked = level;	// we keep track of the highest predecessor we've locked
                    valid = !pred.marked && !succ.marked && pred.next[level] == succ;
                    	// if the predecessor and successor aren't currently in the process of deletion
                    		// and pred.next at current level == the expected successor, we have a valid
                    		// place to perform our insertion
                }
                if (!valid) continue;	// if the place isn't valid for adding (due to deletions, etc)
                							// go back through a new iteration of the while loop to try again
                
                Node<T> newNode = new Node(x, topLevel); 		// made it this far, have a good place to insert the node
                													// create the actual new node
                for (int level = 0; level <= topLevel; level++)	// set successor of the new node at each level
                    newNode.next[level] = succs[level];
                for (int level = 0; level <= topLevel; level++) // set predecessor's next to new node at each level
                    preds[level].next[level] = newNode;
                newNode.fullyLinked = true; // when we've finished creating all the pointers, set fullyLinked
                								// to true, so other threads know this node is good for use
                return true;				// return true to indicate the node has been successfully added and fully linked
            } finally {
                for (int level = 0; level <= highestLocked; level++)
                    preds[level].unlock();	// unlock the predecessors we locked previously at each level
    }   }    }

    boolean remove(T x) {	// got lazy, going home, will comment this tomorrow if you need it!
        Node<T> victim = null;
        boolean isMarked = false;
        int topLevel = -1;
        Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
        while (true) {
            int lFound = find(x, preds, succs);
            if (lFound != -1) victim = succs[lFound];
            if (isMarked | (lFound != -1 &&
                    (victim.fullyLinked && victim.topLevel == lFound && !victim.marked))) {
                if (!isMarked) {
                    topLevel = victim.topLevel;
                    victim.lock.lock();
                    if (victim.marked) {
                        victim.lock.unlock();
                        return false;
                    }
                    victim.marked = true;
                    isMarked = true;
                }
                int highestLocked = -1;
                try {
                    Node<T> pred, succ;
                    boolean valid = true;
                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds[level];
                        pred.lock.lock();
                        highestLocked = level;
                        valid = !pred.marked && pred.next[level] == victim;
                    }
                    if (!valid) continue;
                    for (int level = topLevel; level >= 0; level--)
                        preds[level].next[level] = victim.next[level];
                    victim.lock.unlock();
                    return true;
                } finally {
                    for (int i = 0; i <= highestLocked; i++)  preds[i].unlock();
                }
            } else return false;
    }   }


    private int randomLevel() {
        long random = (long) (Math.random() * Math.pow(2, MAX_LEVEL));
        return (int) (MAX_LEVEL - Math.log10(random) / Math.log10(2));
    }
    
    public void PrintList() {
    	

    	// print out the entire list :D !
    	for(int i = 0; i <= MAX_LEVEL; i++){
    		boolean printedAtThisLevel = false;	// i use this to not print levels we didn't insert anything on
    		Node<T> current = head;
    		String lvlString = "Level " + i + ": ";
    		while(current.next[i] != tail){ // while we haven't hit the tail of the list, print node out
    			printedAtThisLevel = true;
        		lvlString += current.next[i].item.toString() + ", ";
        		current = current.next[i]; // advance dat pointer
        	}
    		if( ! printedAtThisLevel ){
    			continue; // avoid printing if no nodes to print
    		} else {
    			System.out.println( lvlString );
    		}
    	}
    	
    	//	IF we wanted to only traverse the list, we could reduce the code to
		//	for(int i = 0; i < MAX_LEVEL; i++){
		//		Node<T> current = head;
		//		while(current.next[i] != tail){ // while we haven't hit the tail of the list, print node out
    	//			// perform operation here
		//			current = current.next[i]; // advance dat pointer
		//		}
		//	}

    }

    /* ----- NODE CLASS ----- */
    
    private static final class Node<T> {
        final Lock lock = new ReentrantLock();
        final T item;
        final int key;
        final Node<T>[] next;
        volatile boolean marked = false;		// true if currently being deleted, but thread deleting this hasn't
        											// finished deleting this node yet
        volatile boolean fullyLinked = false;	// true if inserted at all necessary levels, and 
        											// all successor / predecessor pointers are set up at each lvl
        private int topLevel;

        // Constructor for first node
        public Node(int key) { // sentinel node constructor
            this.item = null;
            this.key = key;
            next = new Node[MAX_LEVEL + 1];
            topLevel = MAX_LEVEL;
        }

        // Constructor for all other nodes
        public Node(T x, int height) {
            item = x;
            key = x.hashCode();
            next = new Node[height + 1];
            topLevel = height;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }
    }
}