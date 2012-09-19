package org.glue.modules.hadoop.pig.counters


class ProjectionNodeUtil {

	
	static parseFromText(instance, String text){
		
		def split = text.split('As')
		if(split.size() == 1){
			//check with
			split = text.split('With')
			if(split.size() == 1){
				instance.column = instance.alias = text
				instance.function = ''
			}else{
				instance.column = instance.alias = split[0]
				instance.function = split[1]
			}
			
		}else{
		   //we have an alias
		   //look for a With
		   def column = split[0]
		   def alias = split[1]
		   def function = ''
		   def split2 = alias.split('With')
		   if(split2.size() == 2){
				alias = split2[0]
				function = split2[1]
		   }
		
		   instance.column = column
		   instance.alias = alias
		   instance.function = function
		}
			
		return instance
	}
	
}
