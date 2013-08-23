package org.glue.modules.test;

import static org.junit.Assert.*
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import java.sql.ResultSet

import org.glue.modules.SqlModule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * 
 * Tests the SqlModule implementation.
 *
 */
class SqlModuleTest {

	private static final String url = 'jdbc:hsqldb:mem:SqlModuleTest'
	private static final String user = 'sa'
	private static final String pwd = ''
	private static final String driver = 'org.hsqldb.jdbcDriver'
	private static final int numberOfRows = 10

	static SqlModule module

	/**
	* Test the execSql method
	*/
   @Test
   public void testExecSql(){


	   boolean success = module.execSql('db1', 'select * from SqlModuleTest')
   	   assertTrue(success)
	   
   }

	/**
	 * Test that we can iterate over a result with the eachSqlResult method
	 */
	@Test
	public void testEachSqlResult(){

		int count = 0

		module.eachSqlResult'db1', 'select * from SqlModuleTest', { row -> 
			count++; 
			
			int length=row.getMetaData().getColumnCount();
				
				for(def i=0;i<length;i++){
					
					println row[i].getClass()
					
					
				}
		}

		assertEquals(numberOfRows, count)
	}
	
	@Test
	public void testEachSqlResultCollection(){

		int count = 0
		
		def coll = module.eachSqlResult('db1', 'select * from SqlModuleTest')
        coll.each { rs -> count++; println "Element $count : $rs" }

		assertEquals(numberOfRows, count)
	}

	/**
	 * Test that the load file works as expected
	 */
	@Test
	public void testLoadFile(){

		String fileName = module.loadSql( 'db1', 'select * from SqlModuleTest')
		File file = new File(fileName)

		try{

			assertNotNull(fileName)
			assertTrue(file.exists())

			int count = 0
			file.eachLine { count++ }
			assertEquals(numberOfRows, count)
		}finally{
			file.delete()
		}
	}

	@AfterClass
	public static final void shutdown() throws Exception{

		module.destroy()
	}


	@BeforeClass
	public static final void setup() throws Exception{

		//--------- Setup database
		Sql sql = Sql.newInstance(url, user, pwd, driver)
		sql.execute """
		
		  create table SqlModuleTest(
		     id integer not null,
		     name varchar(50)
		  )  
		
		"""

		(1..numberOfRows).each { int i ->

			sql.execute "insert into SqlModuleTest (id, name) values(?,?)", i, "myName_$i"
		}

		// instantiate and configure the SqlModule
		module = new SqlModule()

		module.init(
				new ConfigSlurper().parse("""
			
			 db{
			    db1{
			      host="$url"
			      user="$user"
			      pwd="$pwd"
			      driver="$driver"
			    }
			 }
			
			""")
				)

	}
}
