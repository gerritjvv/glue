package org.glue.modules.hadoop.pig.counters

@Typed
class Counter {

	def column
	def alias
	def function
	
	public Counter(){}
	public Counter(String text){
		ProjectionNodeUtil.parseFromText(this, text)
	}
	
}
