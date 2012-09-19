package org.glue.modules

import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigObject

import java.util.Map

import javax.inject.Inject
import javax.mail.*
import javax.mail.internet.*

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * This module can be used by work flows to launch and monitor work flows.
 *
 */
@Typed(TypePolicy.DYNAMIC)
class DBCountersModule implements GlueModule {

	@Inject
	SqlModule sqlModule

	def defaultConfiguration="";
	ConfigObject config;
	GlueContext context;

	boolean useMysql = false;

	String unitId;

	void destroy(){
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true;
	}

	@Override
	public void configure(String unitId, ConfigObject config) {
		this.unitId = unitId;
	}

	@Override
	public String getName() {
		return "dbCounters";
	}

	@Override
	public void init(ConfigObject config) {
		this.config=config;

		if(config.useMysql){
			useMysql = Boolean.parseBoolean(config.useMysql.toString())
		}
	}

	/**
	 * A SimpleTemplate is created that will write the sql insert logic.<br/>
	 * This method loads the data line by line from an InputStream<br/>
	 * Each line is passed to the lineParse closure that should return a map.<br/>
	 * Each map is sent to the template to generate a view, and the result is an sql insert statemnt.<br/>
	 * The statement is written to a file as a new line.
	 * @param input any object that supports eachLine(Closure). This stream contains the counters data
	 * @param lineParser Closure a closure that takes a String as an argument and returns a Map. The maps should fit the variable names.
	 * @param dbName String the SqlModule configured database name
	 * @param table String the table to load the data to
	 * @param groups List of List, each index contains a List of size == 2. index 0 == group name, index=1 enclosing parameters (''' or other).
	 * @param counters The counter objects that support arithmetic functions e.g. integer, double, float etc.
	 * @param onUpdateCounters The counters to update if the record already exists.
	 * @param int rows inserted
	 */
	@Typed(TypePolicy.DYNAMIC)
	public int loadCounters(input, Object protobuff, String dbName, String table,
	List<List> groups, List<String> counters, List<String> onUpdateCounters, insertOnDuplicate=true){

	    //Here we wrap the protobuff object arround a lineParser closure
		return loadCounters(input, { line ->
			return protobuff.parseFrom(((byte[])line.decodeBase64())).getProperties()
		}, dbName, table,
		groups, counters, onUpdateCounters, insertOnDuplicate)
		
	}


	/**
	*
	*	map.each({ columnName, arr ->
				
				if(columnName == 'table'){
					table = columnName
				}
				
				if(arr[0] == 'counter'){
					counters << columnName;
					onUpdatecounters << columnName;
				}else if(arr[0] == 'group'){
					groups << columnName;
				}
				
				if(arr[1] == 'str')
					valueMap[columnName] = [arr[2], '\''];
				else
					valueMap[columnName] = [arr[2], ''];
				
			})
		
	* lineParser should return a map.
	*  [ 'table':'<tablename>', 'mycol':['<counter>|<group>','<str|int>',<value>] ]
	* 
	*/
	@Typed(TypePolicy.DYNAMIC)
	public int loadTypeReportCounters(input, Closure lineParser, String dbName, boolean insertOnDuplicate=true){

		//create temporary file to store sql
		File logFile = context?.getLogger()?.getLogFile();
		File parentDir = (!logFile || !logFile.exists() ) ? new File("/tmp/") : logFile.parentFile
		String fileName = "table_" + System.currentTimeMillis() + ".sql"
		File file = new File(
			parentDir, fileName
		)

		//create the template
		//key = type/table values = [engine, template]
		def templateMap = [:]
		def engine = new SimpleTemplateEngine()

		def skipMap = null
		
		
		
		def parseLine = { line ->
			def map = lineParser(line.toString().replaceAll('\'', '').replaceAll('\"', ''))
			
			def groups = [] as HashSet
			def counters = [] as HashSet
			def onUpdatecounters = [] as HashSet
			def valuesMap = [:]
			
			if(map == null){
				return	
			}
			
			def table = null
			def valueMap = [:]
			
			map.each({ columnName, arr ->
				
				if(columnName == 'table'){
					table = arr
				}
				
				def groupArr = []
				
				if(arr[0] == 'counter'){
					counters << columnName;
					onUpdatecounters << columnName;
				}else if(arr[0] == 'group'){
					groupArr << columnName;
						
					
					if(arr[1] == 'str') groupArr << '\''
					else groupArr << ''
					
					groups << groupArr	
				}
				
				
				valueMap[columnName] = arr[2];
				
			})
			
			if(!(table && groups && counters && valueMap))
				throw new RuntimeException("Missing values table,groups,counters table:$table, groups:$groups, counters:$counters valueMap:$valueMap")
			
			valueMap['table'] = table
			
			def template = templateMap[table]
			if(!template){
				template = engine.createTemplate(insertTableSql(table, groups, counters, onUpdatecounters, insertOnDuplicate as Boolean ))
				templateMap[table] = template;
			}
			
			def templateresult = template.make(valueMap) as String
			return templateresult
		}

		//For each line, parse with lineParser to get map.
		// pass through template
		// write line to file
		int counter = 0
		if(useMysql){
			println "Using mysql client"
			file.withWriter { writer ->
				input.eachLine { line ->
					
					def parsedLine = parseLine(line)
					if(parsedLine){	writer.writeLine(parsedLine); counter++; }
				}
			}

			sqlModule.mysqlImport(dbName, file)

		}else{
			println "Using jdbc batch"
			sqlModule.withSql  dbName, { sql ->
				sql.withBatch( { st ->
					input.eachLine { line ->
					
						def parsedLine = parseLine(line)
						if(parsedLine){	st.addBatch(parsedLine); counter++; }
					}
				})
			}


		}

		return counter;
	}
	
	/**
	 * A SimpleTemplate is created that will write the sql insert logic.<br/>
	 * This method loads the data line by line from an InputStream<br/>
	 * Each line is passed to the lineParse closure that should return a map.<br/>
	 * Each map is sent to the template to generate a view, and the result is an sql insert statemnt.<br/>
	 * The statement is written to a file as a new line. 
	 * @param input any object that supports eachLine(Closure). This stream contains the counters data
	 * @param lineParser Closure a closure that takes a String as an argument and returns a Map. The maps should fit the variable names.
	 * @param dbName String the SqlModule configured database name
	 * @param table String the table to load the data to, if table is null the line parser map must contain a variable _tableName
	 * @param groups List of List, each index contains a List of size == 2. index 0 == group name, index=1 enclosing parameters (''' or other).
	 * @param counters The counter objects that support arithmetic functions e.g. integer, double, float etc.
	 * @param onUpdateCounters The counters to update if the record already exists.
	 * @param int rows inserted
	 */
	@Typed(TypePolicy.DYNAMIC)
	public int loadCounters(input, Closure lineParser, String dbName, String table,
	List<List> groups, List<String> counters, List<String> onUpdateCounters, insertOnDuplicate=true){

		//create temporary file to store sql
		File logFile = context?.getLogger()?.getLogFile();
		File parentDir = (!logFile || !logFile.exists() ) ? new File("/tmp/") : logFile.parentFile
		String fileName = "table_" + System.currentTimeMillis() + ".sql"
		File file = new File(
			parentDir, fileName
			)


		//create the template
		String sqlInsert = insertTableSql(table, groups, counters, onUpdateCounters, insertOnDuplicate)
		def engine = new SimpleTemplateEngine()
		def template = engine.createTemplate(sqlInsert)


		//For each line, parse with lineParser to get map.
		// pass through template
		// write line to file
		int counter = 0
		if(useMysql){
			println "Using mysql client"
			file.withWriter { writer ->
				input.eachLine { line ->
					def map = lineParser(line)
					map['table'] = table
					def parsedLine = template.make(map) as String
					if(parsedLine){	writer.writeLine(parsedLine); counter++; }
				}
			}

			sqlModule.mysqlImport(dbName, file)

		}else{
			println "Using jdbc batch"
			sqlModule.withSql  dbName, { sql ->
				sql.withBatch( { st ->
					input.eachLine { line ->
						def map = lineParser(line)
						map['table'] = table
						def parsedLine = template.make(map).toString()
						if(parsedLine){	st.addBatch(parsedLine); counter++; }
					}
				})
			}


		}

		return counter;
	}

	@Override
	public void onProcessKill(GlueProcess process, GlueContext context){
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,
	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
		this.context = context
	}

	@Override
	public Map getInfo() {
		return config;
	}



	/**
	 * Converts a string of format my_str to myStr
	 * @param str
	 * @return String
	 */
	@Typed(TypePolicy.DYNAMIC)
	private static final String camelCase(str){
		int i = 0
		return str.split("_").collect{ word ->
			if(i++ != 0)
				word.toLowerCase().capitalize()
			else
				word.toLowerCase()
		}.join("")
	}

	private static final String  insertTableSql(String table, groups, counters, onUpdateCounters , boolean insertOnDuplicate){

		def groupSet = [] as HashSet
		groupSet = groups.sort({it[0]})
		
		groups = groups.collect({ it[0] }).sort();
		
		
		def counterSet = counters
		def onUpdateCountersSet = onUpdateCounters

		final StringBuilder groupValues = new StringBuilder()
		groupSet.eachWithIndex { v, int i ->
			if(i != 0)
				groupValues.append(',')

			groupValues.append("${v[1]}\${${ v[0] }?.replaceAll(\"[^0-9a-zA-Z @,-;_?\\\$()?.!*+:]\", \"\")}${v[1]}")
		}

		StringBuilder counterValues = new StringBuilder()
		counterSet.eachWithIndex { String v, int i ->
			if(i != 0) counterValues.append(',')
			counterValues.append("\${${v}}")
		}

		StringBuilder onDuplicateUpdates = new StringBuilder()

		counters.intersect(onUpdateCounters).eachWithIndex { onDuplicateCounter, i ->
			if(i != 0) onDuplicateUpdates.append(", ")
			onDuplicateUpdates.append("$onDuplicateCounter = $onDuplicateCounter + VALUES($onDuplicateCounter)")
		}

		StringBuilder str = new StringBuilder()

		if(!table)
			table = '${_tableName}'
			
		if(insertOnDuplicate){
			str.append( """
				   insert into ${table} (${groups.join(',')},${counterSet.join(',')}) values (${groupValues}, ${counterValues} )
				   ON DUPLICATE KEY UPDATE ${onDuplicateUpdates};
					""")
			
		}else{
			str.append("""
		     update ${table} SET 
		  """)

			counterSet.eachWithIndex { counter, i ->
				if(i != 0) str.append(', ')

				str.append("${counter}=\${${counter}}")
			}
			str.append(" WHERE ")
			groups.eachWithIndex {  v, int i ->
				if(i != 0) str.append(' AND  ')
				str.append("${v[0]}=${v[1]}\${${v[0]}}${v[1]}")
			}

		}
		println "SQL: $str"
		return str;
	}
}
