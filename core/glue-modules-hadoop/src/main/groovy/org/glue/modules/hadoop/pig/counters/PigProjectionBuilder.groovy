package org.glue.modules.hadoop.pig.counters

import groovy.util.BuilderSupport

import java.util.Map

@Typed(TypePolicy.DYNAMIC)
class PigProjectionBuilder extends BuilderSupport{



	List<Projection> projections = []

	Load load
	int parallel = 10

	TypeReport typeReport
	
	Generate generate

	Projection current

	def stack = []

	public String toString(){
		PigScriptWriter.write(load, parallel, projections, typeReport, generate)
	}

	protected void setParent(Object parent, Object child){
	}

	protected Object createNode(Object name){
		if(stack.size() == 0){
			current = new Projection(name)
			projections << current
			stack << current
		}else{
			throw new RuntimeException("$name is not expected here")
		}

		return current
	}

	protected Object createNode(Object name, Object value){
		if(!current){
			name = name.toString().trim()
			switch(name){
				case "load":
					load = new Load(value.toString())
					break
				case "parallel":
					parallel = Integer.parseInt(value.toString())
					break
				case "typeReport":
				    typeReport = new TypeReport(value.toString())
					break
				case "generate":
				 	generate = new Generate(value.toString())
					break
				default:
					throw new RuntimeException("$name is not expected here")
			}
		}else{
			switch(name){
				case "generate":
				if(value instanceof Collection){
					value.each { v ->
						current.generates << new Generate(v.toString())
					}
				}else{
					current.generates << new Generate(value.toString())
				}
				break
				case "group":
					if(value instanceof Collection){
						value.each { v ->
							current.groups << new Group(v.toString())
						}
					}else{
						current.groups << new Group(value.toString())
					}
					break
				case "counter":
					if(value instanceof Collection){
						value.each { v ->
							current.counters << new Counter(v.toString())
						}
					}else{
						current.counters << new Counter(value.toString())
					}
					break
				case "filter":

					if(value instanceof Collection){
						value.each { v ->
							current.filters << new Filter(expression:v.toString())
						}
					}else{
						current.filters << new Filter(expression:value.toString())
					}

					break
				case "order":

					if(value instanceof Collection){
						value.each { v ->
							current.orders << new Order(expression:v.toString())
						}
					}else{
						current.orders << new Order(expression:value.toString())
					}

					break

				default:
					throw new RuntimeException("$name not expected here")
			}

			stack << name
		}

		return current
	}

	protected Object createNode(Object name, Map attributes){
		switch(name){
			case "properties":
			    if(attributes['name'])
					current.store.name = attributes['name']
				
			    if(attributes['reader'])
					current.store.reader = attributes['reader'] as Closure
					
					
				break
			
			default:
				throw new RuntimeException("$name not expected here")
		}
	}

	protected Object createNode(Object name, Map attributes, Object value){
	}

	protected void nodeCompleted(Object parent, Object node) {
		if(stack.size() > 0) stack.pop()
	}
}
