package com.heymitch.skiplist;
import java.util.*;

public class MainClass
{
	
	public static void main(String[] args) 
	{
		ConcurrentSkipList<Integer> skipList;
		ConcurrentTreeMap<Integer, Integer> treeMap;
		
		ArrayList threadList = new ArrayList<ProgramThread>();	// arrayList of ProgramThread threads
		
		int numThreads = 8;
		int numOperations = 1000000;		// 1 MEEELEEEON OPERATIONS
		Iterator<ProgramThread> threadItr;
		
		long startTime, duration;
		long skipListTime = 0;
		long treeMapTime = 0;
		
		int iterations = 10;
		
		// INITIALIZE DATASETS
		String[] tests = {"contains", "add", "database"};
		String testString = "";
		
		for(String test : tests){
			testString += " " + test;
		}
		
		System.out.println("Performing tests " + testString.toUpperCase() + " with " + iterations + " iterations." );
		
		for(String test : tests){
			
			for(int itr = 0; itr < iterations; itr++){
				threadList.clear();
				
				skipList = new ConcurrentSkipList<Integer>();
				treeMap = new ConcurrentTreeMap<Integer, Integer>();
				
				for(int i = 0; i <= 100000; i++) { // add 0 through 100,000 to each set
					skipList.add(i);
					treeMap.put(i,i);
				}
				
				// test the skiplist!
				for(int i = 0; i < numThreads; i++){
					ProgramThread newThread = new ProgramThread(skipList, i, numOperations/numThreads, "contains");
				    threadList.add(newThread);									// add it to the array list
				}
				
				threadItr = threadList.iterator();
				
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
				skipListTime += duration;
				
				
				
				
				// test the treemap!
				threadList.clear();
				
				for(int i = 0; i < numThreads; i++){
					ProgramThread newThread = new ProgramThread(treeMap, i, numOperations/numThreads, test);
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
				treeMapTime += duration;
				
			}
			
			System.out.println("Avg time " + test.toUpperCase() 
					+ " -- Skiplist: " + skipListTime / iterations / 1000000 
					+ "ms -- TreeMap: " + treeMapTime / iterations / 1000000 + "ms" );
			
			
		}

		
		
		
		
	}
}

