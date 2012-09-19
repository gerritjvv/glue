package org.glue.test.unit.process

/**
 *
 * Simple java program that prints OK.
 *
 */
class NeverEndProgram {

	public static void main(String[] args){
		
		while(true){
			Thread.sleep(1000L)
			println "Never end program running"	
		}
		
	}
}
