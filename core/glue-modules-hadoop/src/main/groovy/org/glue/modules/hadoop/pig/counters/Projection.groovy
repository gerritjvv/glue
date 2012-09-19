package org.glue.modules.hadoop.pig.counters

class Projection {

	
	List<Group> groups = []
	List<Counter> counters = []
	List<Filter> filters = []
	List<Order> orders = []
	List<Generate> generates = []
	
	Store store;
	
	
	public Projection(){}
	
	public Projection(String text){
		def split = text.split('Using')
		store = new Store()
		if(split.size() == 1){
		   store.path = split[0]
		   store.function = 'PigStorage()'		
		}else{
		   store.path = split[0]
		   store.function = split[1]
		}	
	}
	
}
