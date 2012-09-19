package org.glue.test.modules.hadoop.pig.counters

import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.glue.modules.hadoop.PigCountersModule
import org.glue.modules.hadoop.PigModule
import org.glue.unit.om.GlueContext
import org.junit.After
import org.junit.Before
import org.junit.Test

class PigCountersModuleTest{

	File baseDir = new File('target/test/PigCountersModuleTest')
	PigModule pigModule
	PigCountersModule pigCountersModule

	File pigOutputFile = null

	@Test
	public void testPigCounters(){
		//public void countersToDB( GlueContext context, String dbName, Closure closure )

		GlueContext context;

		pigCountersModule.run(context, { builder ->
			builder.load('src/test/resources/pigModuleTest/testdata/data1.csvUsingPigStorage(\',\')Asa:chararray,hits:int'){
				//src/test/resources/pigModuleTest/testdata/data1.txt
				parallel(1)
				"${baseDir.path}/testPigCounters"(){
					group( ['a'])
					counter  ( ['hitsWithSUM'])
				}
			}

		})

		File result = new File("${baseDir.path}/testPigCounters")
		assertEquals(2, result.list().size())

		def dataMap = [:]
		result.list().each { String file ->

			if(file.startsWith("part")){

				new File(result,file).eachLine{ String line ->
					def split = line.split('\t')
					dataMap[split[0]] = split[1]
				}

			}

		}

		assertEquals('3', dataMap['a'])
		assertEquals('1', dataMap['b'])

	}

	@Before
	public void setUp() throws Exception {
		pigModule = new PigModule()

		pigModule.init(
				new ConfigSlurper().parse("""
			
			clusters{
					type1hadoopcluster{
						isDefault=true
						pigProperties="src/test/resources/pigModuleTest/type1.properties"
					}
				}
				
			classpath = [ ${getClassPathString()} ]
			""")
				)

		pigCountersModule = new PigCountersModule(pig:pigModule)

		if(baseDir.exists())
			FileUtils.deleteDirectory(baseDir)

		baseDir.mkdirs()
	}



	@After
	public void tearDown() throws Exception {

	}


	/**
	 * Gets the jars from:<br/>
	 * <ul>
	 *  <li>the class path parameter java.class.path</li>
	 *  <li>target/classes</li>
	 *  <li>target/test-classes</li>
	 *  <li>src/test/resources</li>
	 * </ul>
	 *
	 * @return String
	 */
	String getClassPathString(){
		String classpath = System.getProperty("java.class.path")

		def sep = ','

		String pathStr = ""
		classpath.split(File.pathSeparator).eachWithIndex { str, int i ->
			if(i != 0) pathStr += sep

			pathStr += "\'$str\'"
		}

		pathStr += "$sep\'${new File('target/classes').absolutePath}\'"
		pathStr += "$sep\'${new File('target/test-classes/').absolutePath}\'"
		pathStr += "$sep\'${new File('src/test/resources').absolutePath}\'"

		return pathStr
	}
}
