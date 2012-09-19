package org.glue.modules.hadoop

import javax.inject.Inject

import org.glue.modules.hadoop.pig.counters.PigProjectionBuilder
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

class PigCountersModule implements GlueModule{

	@Inject
	PigModule pig
//	
//	@Inject
//	DBCountersModule dbCounters
	
//	@Inject
//	HDFSModule hdfsModule
	
//	public void countersGPBToDB( GlueContext context, String dbName, protoBuff, Closure closure ){
//		
//		PigProjectionBuilder builder = new PigProjectionBuilder()
//		closure(builder)
//		
//		String pigScript = builder.toString()
//		if(pig.run(context, pigScript)){
//			
//   		   builder.projections.each { Projection p ->
//				
//				def path = p.store.path
//				
//				def groups = [], counters = [], updateCounters;
//				
//				p.groups.each { Group g ->  groups << [g.alias, '\''] }
//				p.counters.each { Counter c -> counters << c.alias; updateCounters << c.alias; }
//				
//				dbCounters.loadCounters(
//						[eachLine: { Closure c -> hdfsModule.eachLine(path, { line -> c(line) } )  } ],
//						protoBuff,
//						dbName,
//						p.store.name,
//						groups,
//						counters,
//						updateCounters
//					)
//				
//			}
//			
//		}else{
//			 throw new RuntimeException("Failed to run Pig")
//		}
//		
//	}
	
	/**
	 * Used to run a pig script using the PigProjectionBuilder
	 * @param context
	 * @param closure this closure will be called with the PigProjectionBuilder as argument
	 */
	public void run( GlueContext context, Closure closure ){
		
		PigProjectionBuilder builder = new PigProjectionBuilder()
		closure(builder)
		
		String pigScript = builder.toString()
		if(!pig.run(context, pigScript)){
			 throw new RuntimeException("Failed to run Pig")
		}
		
	}
	
	/**
	* Used to run a pig script using the PigProjectionBuilder
	* @param context
	* @param closure this closure will be called with the PigProjectionBuilder as argument
	*/
   public String createPigScript( GlueContext context, Closure closure ){
	   
	   PigProjectionBuilder builder = new PigProjectionBuilder()
	   closure(builder)
	   
	   return builder.toString()
   }
   
   
//	/**
//	 * Used to set counters to a database directly
//	 * @param context
//	 * @param dbName Database name
//	 * @param closure this closure will be called with the PigProjectionBuilder as argument
//	 */
//	public void countersToDB( GlueContext context, String dbName, Closure closure ){
//		
//		PigProjectionBuilder builder = new PigProjectionBuilder()
//		closure(builder)
//		
//		String pigScript = builder.toString()
//		if(pig.run(context, pigScript)){
//			
//			//public int loadCounters(input, Closure lineParser, String dbName, String table,
//			//	List<List> groups, List<String> counters, List<String> onUpdateCounters, insertOnDuplicate=true)
//	        builder.projections.each { Projection p ->
//				
//				def path = p.store.path
//				
//				def eachLineClosure = (p.store.reader) ? { line -> p.store.reader(line) } : { line -> line } 
//				
//				def groups = [], counters = [], updateCounters;
//				
//				p.groups.each { Group g ->  groups << [g.alias, '\''] }
//				p.counters.each { Counter c -> counters << c.alias; updateCounters << c.alias; }
//				
//				dbCounters.loadCounters( 
//						[eachLine: { Closure c -> hdfsModule.eachLine(path, { line -> c(line) } )  } ],
//						eachLineClosure,
//						dbName,
//						p.store.name,
//						groups,
//						counters,
//						updateCounters
//					)
//				
//	        }		
//			
//		}else{
//		 	throw new RuntimeException("Failed to run Pig")
//		}
//		
//	}
	
	void configure(String unitId, ConfigObject config){
	
	}
	
	void init(ConfigObject config){
		
	}
	
	void onUnitStart(GlueUnit unit, GlueContext context){
		
	}
	void onUnitFinish(GlueUnit unit, GlueContext context){
		
	}
	void onUnitFail(GlueUnit unit, GlueContext context){
		
	}

	Boolean canProcessRun(GlueProcess process, GlueContext context){
		return true;
	}
	void onProcessStart(GlueProcess process,GlueContext context){}
	void onProcessFinish(GlueProcess process, GlueContext context){}
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t){}
	
	void onProcessKill(GlueProcess process, GlueContext context){}
	
	String getName(){ "PigCounters" }
	
	void destroy(){}
	
	public Map getInfo(){[:]}
	
}
