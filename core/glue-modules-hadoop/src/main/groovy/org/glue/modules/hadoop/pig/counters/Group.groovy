package org.glue.modules.hadoop.pig.counters

@Typed
class Group {

	def column
	def alias
	def function
	
	public Group(){}
	public Group(String text){
		ProjectionNodeUtil.parseFromText(this, text)
	}
	
}
