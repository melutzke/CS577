package com.heymitch.skiplist;

import java.util.Random;

/*
 * ProgramThread
 *  a thread that does 10,000 random contains test on the concurrent skiplist
 *  I start 10 of these threads above
 */
class ProgramThread extends Thread
{
	ConcurrentSkipList skipSet;
	ConcurrentTreeMap treeSet;
	Random rand = new Random();
	String type;
	int num;
	int numberOfOperations;
	String dataType;

	public ProgramThread(ConcurrentSkipList argSkipList, int number, int numOps, String testType){
		super();
		skipSet = argSkipList;
		num = number;
		numberOfOperations = numOps;
		type = testType;
		dataType = "SkipList";
	}

	public ProgramThread(ConcurrentTreeMap argTreeMap, int number, int numOps, String testType){
		super();
		treeSet = argTreeMap;
		num = number;
		numberOfOperations = numOps;
		type = testType;
		dataType = "TreeMap";
	}

	public void run ()
	{	
		if( type == "contains" ){
			for(int i = 0; i < numberOfOperations; i++){
				int randKey = rand.nextInt(100000);
				if( skipSet != null ){
					skipSet.contains( randKey );
				} else {
					treeSet.containsKey( randKey );
				}
			}
		} 
		else if(type == "add") {
			for(int i = 0; i < numberOfOperations; i++) {
				if( skipSet != null ){
					skipSet.add( i );
				} else {
					treeSet.put( i, i );
				}
			}
		}
		else {
			System.out.println("Invalid argument");
		}


		System.out.println("Test: " + dataType + ", " + type + ", Thread finishing: " + this.num);
	}
}