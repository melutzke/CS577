package com.heymitch.skiplist;

import java.util.*;

public class MainClass
{
	
	public static void main(String[] args) 
	{
		ConcurrentSkipList<Integer> skipList = new ConcurrentSkipList<Integer>();
		
		for(int i = 0; i <= 10000; i++) {
			skipList.add(i);
		}
		
		ArrayList threadList = new ArrayList<MyThread>();
		
		
		for(int i = 0; i < 10; i++){
			MyThread mt = new MyThread (skipList, i);
		    threadList.add(mt);
		}
		
		Iterator<MyThread> threadItr = threadList.iterator();
		
		long startTime = System.nanoTime();
		
		while( threadItr.hasNext() ){
			try {
				MyThread current = threadItr.next();
				current.start();
				current.join();
			} catch (InterruptedException e) {
				System.out.println("Interrupted, but nobody cares :D");
			}
		}
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1000000;
		
		System.out.println("It took " + duration + "ms");
		
	    //skipList.PrintList();

	}
}


class MyThread extends Thread
{
   ConcurrentSkipList dataSet;
   Random rand = new Random();
   int num;
   public MyThread(ConcurrentSkipList skipList, int number){
	   super();
	   dataSet = skipList;
	   num = number;
   }
   public void run ()
   {
	    for(int i = 0; i < 10000; i++){
	    	dataSet.contains(rand.nextInt(10000));
	    }
	    System.out.println("Thread finishing: " + this.num);
   }
}