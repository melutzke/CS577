package com.heymitch.skiplist;

public class MainClass
{
	
	public static void main(String[] args) 
	{
		ConcurrentSkipList<Integer> skipList = new ConcurrentSkipList<Integer>();
		
		for(int i = 1; i <= 10000; i++) {
			skipList.add(i);
		}
		
		skipList.PrintList();
	}
}
