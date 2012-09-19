package org.glue.modules.hadoop.pig.counters

class Load {

	def path
	def function
	def schema
	
	public Load(){}
	public Load(String text){
		boolean hasUsing = false, hasAs = false
		
		hasUsing = text.contains('Using')
		hasAs = text.contains('As')
		
		if(hasUsing && hasAs){
			def split = text.split('Using')
			path = split[0]
			split = split[1].split('As')
			function = split[0]
			schema = split[1]	
		}else if(hasUsing){
			def split = text.split('Using')
			path = split[0]
			function = split[1]
		}else if(hasAs){
			def split = text.split('As')
			path = split[0]
			schema = split[1]
		}else{
			path = text
			function = 'PigStorage()'
		}
		
	}
	
}
