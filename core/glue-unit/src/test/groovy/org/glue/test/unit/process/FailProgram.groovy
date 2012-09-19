package org.glue.test.unit.process

/**
 * This program will always fail
 *
 */
class FailProgram {

	public static void main(String[] args){
		throw new RuntimeException("Induced Error")
	}
	
}
