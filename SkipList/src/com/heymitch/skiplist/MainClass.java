package com.heymitch.skiplist;

import java.util.*;

public class MainClass
{
	
	public static void main(String[] args) 
	{
		ConcurrentSkipList<Integer> skipList = new ConcurrentSkipList<Integer>();
		ConcurrentTreeMap<Integer, Integer> treeMap = new ConcurrentTreeMap<Integer, Integer>();
		
		ArrayList threadList = new ArrayList<ProgramThread>();	// arrayList of ProgramThread threads
		
		int numThreads = 8;
		int numOperations = 100000;
		
		long startTime, duration;
		
		for(int i = 0; i <= 100000; i++) { // add 0 through 10,000 to each set
			skipList.add(i);
			treeMap.put(i,i);
		}
		
		// test the skiplist!
		
		for(int i = 0; i < numThreads; i++){
			ProgramThread newThread = new ProgramThread(skipList, i, numOperations/numThreads, "contains");
		    threadList.add(newThread);									// add it to the array list
		}
		
		Iterator<ProgramThread> threadItr = threadList.iterator();
		
		startTime = System.nanoTime();
		
		while( threadItr.hasNext() ){							// start each thread
			ProgramThread current = threadItr.next();
			current.start();
		}
		
		threadItr = threadList.iterator();
		while( threadItr.hasNext() ){	
			try {												// call join on it so main waits for it
				ProgramThread current = threadItr.next();
				current.join();
			} catch (InterruptedException e) {
				System.out.println("Interrupted, but nobody cares :D");
			}
		}

		duration = (System.nanoTime() - startTime);

		System.out.println("Test took " + duration + " ns");
		
		
		
		
		// repeat process with treeMap
		threadList.clear();
		
		for(int i = 0; i < numThreads; i++){
			ProgramThread newThread = new ProgramThread(treeMap, i, numOperations/numThreads, "contains");
		    threadList.add(newThread);									// add it to the array list
		}
		
		startTime = System.nanoTime();
		
		threadItr = threadList.iterator();
		while( threadItr.hasNext() ){							// start each thread
			ProgramThread current = threadItr.next();
			current.start();
		}
		
		threadItr = threadList.iterator();
		while( threadItr.hasNext() ){	
			try {												// call join on it so main waits for it
				ProgramThread current = threadItr.next();
				current.join();
			} catch (InterruptedException e) {
				System.out.println("Interrupted, but nobody cares :D");
			}
		}

		duration = (System.nanoTime() - startTime);

		System.out.println("Test took " + duration + " ns");
		
		
		
		
		
	}
}

