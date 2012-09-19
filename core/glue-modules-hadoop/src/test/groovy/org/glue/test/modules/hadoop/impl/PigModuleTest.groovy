package org.glue.test.modules.hadoop.impl

import static org.junit.Assert.*

import org.glue.modules.hadoop.PigModule
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueContextBuilder
import org.glue.unit.om.impl.DefaultGlueUnitBuilder;
import org.glue.unit.om.impl.GlueUnitImpl
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * 
 * Tests that the pig module can run simple pig queries
 *
 */
class PigModuleTest {

	PigModule pigModule
	
	/**
	 * We test that the pig module can run pig script files.
	 */
	@Test
	public void runSimpleQueryPigFile(){
		def script = 'src/test/resources/pigModuleTest/pigtest.pig'

		boolean success = pigModule.run(getGlueContext('1'), "test1",
				script,
				[INPUT:'src/test/resources/pigModuleTest/testdata/data1.txt'],
				true
				)

		assertTrue(success)
	}

	/**
	 * Test the auto registration of jar files throw an exception if the jar cannot be found.
	 */
	@Test(expected=ModuleConfigurationException)
	public void testRegisterJarNotFound(){
  
     
		PigModule pigModule = new PigModule()

		pigModule.init(
				new ConfigSlurper().parse("""
				   
				   clusters{
						   type1hadoopcluster{
							   isDefault=true
							   pigProperties="src/test/resources/pigModuleTest/type1.properties"
						   }
				   }


			       classpath = [ ${getClassPathString()} ]
			
				   jars = [
						'src/test/resources/pigModuleTest/notexists.jar'
				   ]
						   
				   """)
				)


		def script = """
	   
		   a = LOAD '\$INPUT' as (p:chararray, n:int);
		   g = group a by p;
		   r = foreach g generate group;
		   dump r;
	   
	   """
		//if the jars cannot be found pig will throw an error
		pigModule.run(getGlueContext('1'), "test1",
				script,
				[INPUT:'src/test/resources/pigModuleTest/testdata/data1.txt'],
				true
				)

     
	}

	/**
	 * Test the auto registration of jar files
	 */
	@Test
	public void testRegisterJars(){

		pigModule.configure('123',
				new ConfigSlurper().parse("""
		   
		   jars = [
		     'src/test/resources/pigModuleTest/test1.jar',
		     'src/test/resources/pigModuleTest/test1.jar'
		   ]
		   
		   """)
				)

		def script = """
	   
		   a = LOAD '\$INPUT' as (p:chararray, n:int);
		   g = group a by p;
		   r = foreach g generate group;
		   dump r;
	   
	   """
		//if the jars cannot be found pig will throw an error
		boolean success = pigModule.run(getGlueContext('1'), "test1",
				script,
				[INPUT:'src/test/resources/pigModuleTest/testdata/data1.txt'],
				true
				)

		assertTrue(success)
	}

	/**
	 * We test a simple query with parameters
	 */
	@Test
	public void runSimpleQueryWithParameters(){
		def script = """
	   
		   a = LOAD '\$INPUT' as (p:chararray, n:int);
		   g = group a by p;
		   r = foreach g generate group;
		   dump r;
	   
	   """

		boolean success = pigModule.run(getGlueContext('1'), "test1",
				script,
				[INPUT:'src/test/resources/pigModuleTest/testdata/data1.txt'],
				true
				)

		assertTrue(success)
	}
	/**
	 * We test a simple query with parameters
	 **/
	@Test
	public void runSimpleQueryWithParametersContainingSpaces(){
		def script = """
	   
		   a = LOAD '\$INPUT' as (\$fields);
		   g = group a by p;
		   r = foreach g generate group;
		   dump r;
	   
	   """

		boolean success = pigModule.run(getGlueContext('1'), "test1",
				script,
				[INPUT:'src/test/resources/pigModuleTest/testdata/data1.txt',fields: 'p:chararray, n:int'],
				true
				)

		assertTrue(success)
	}

	/**
	 * We test a simple query that should not fail.
	 */
	@Test
	public void runSimpleQuery(){
		def script = """
		
			a = LOAD 'src/test/resources/pigModuleTest/testdata/data1.txt' as (p:chararray, n:int);
			g = group a by p;
			r = foreach g generate group;
			dump r;
		
		"""

		boolean success = pigModule.run(getGlueContext('1'), "test1",
				script,
				[:],
				true
				)

		assertTrue(success)
	}

	/**
	 * We test that a query that will fail returns false.
	 */
	@Test(expected=RuntimeException)
	public void runSimpleQueryFail(){
		
		def script = """
		
			a = LOAD 'doesnotexist' as (p:chararray, n:int);
			g = group a by p;
			r = foreach g generate group;
			dump r;
		
		"""

		//we expect and error here
		pigModule.run(getGlueContext('1'), "test1",
				script,
				[:],
				true
				)
		

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

	@Before
	public void setup()throws Exception{

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
	}

	private GlueContext getGlueContext(String unitId){
		
		GlueUnit unit = new DefaultGlueUnitBuilder().build('''
		 name='test'
		 tasks {
		   p1{
		    tasks={ c -> }
		   } 
		 }
		''')
		GlueContextBuilder builder = new DefaultGlueContextBuilder()
		return builder.build(unitId, unit, [:])
	}
	
	@After
	public void after() throws Exception{
		pigModule.destroy()
	}
}
