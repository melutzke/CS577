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
				int randKey = rand.nextInt(100000);
				if( skipSet != null ){
					skipSet.add( randKey );
				} else {
					treeSet.put( randKey, randKey );
				}
			}
		} else if(type == "database"){
			for(int i = 0; i < numberOfOperations; i++){
				int randOp = rand.nextInt(10) + 1;
				if(randOp >= 8){
					if( skipSet != null){
						skipSet.remove( rand.nextInt( 10000 ) );
					} else {
						treeSet.remove( rand.nextInt( 10000 ) );
					}
				} else if(randOp >= 5){
					if( skipSet != null){
						skipSet.add( rand.nextInt( 10000 ) );
					} else {
						int newInt = rand.nextInt( 10000 );
						treeSet.put( newInt, newInt );
					}
				} else {
					if( skipSet != null){
						skipSet.contains( rand.nextInt( 10000) );
					} else {
						treeSet.containsKey( rand.nextInt( 10000) );
					}
				}
			}
		} else {
			System.out.println("Invalid argument");
		}
	}
}