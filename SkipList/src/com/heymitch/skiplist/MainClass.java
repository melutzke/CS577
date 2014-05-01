package com.heymitch.skiplist;

public class MainClass
{
	
	public static void main(String[] args) 
	{
		SkipList<Integer> skipList = new SkipList<Integer>();
		
		for(int i = 1; i <= 1000; i++) {
			skipList.add(i);
		}
	}
}
