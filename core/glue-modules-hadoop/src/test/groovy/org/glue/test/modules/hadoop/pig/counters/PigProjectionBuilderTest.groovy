package org.glue.test.modules.hadoop.pig.counters

import static org.junit.Assert.*

import org.glue.modules.hadoop.pig.counters.PigProjectionBuilder
import org.glue.modules.hadoop.pig.counters.PigScriptWriter
import org.junit.Test
@Typed(TypePolicy.DYNAMIC)
class PigProjectionBuilderTest {


	@Test
	public void tempTest(){

		def builder = new PigProjectionBuilder()

		builder.load('/log/raw/clicks/year=2011/month=11/day=01Usingcom.twitter.elephantbird.pig.proto.LzoProtobuffB64LinePigStore(\'ad_data\')'){
			parallel(20)
			'/tmp/test1111UsingPigStorage(\',\')'(){
				group( [
					'month',
					'ipinfo.countryAsCountry']
				)
				counter  ( [
					'\$1AstrafficWithCOUNT']
				)
				order ('traffic DESC')
				properties([ reader:{ line -> line}, name:'mytest' ])
			}

		}
		println PigScriptWriter.write(builder.load, builder.parallel, builder.projections, null)
	}
	
	@Test
	public void testBuildScript(){

		def builder = new PigProjectionBuilder()
		builder.load('/logs/typeUsingcom.Loader'){
			parallel(20)
			'/tmp/reports/myprojectionUsingcom.analytics.mystore.Store(\'ads\')'(){
				group( [
					'myVarAsvar1' ,
					'myVar2WithFunction']
				)
				counter  ( [
					'trafficAsImpressionsWith(int)SUM']
				)
				filter ( [
					'traffic > 0 and 1 = 1',
					'traffic == 1']
				)
				order ('traffic DESC')
				properties([ reader:{ line -> line}, name:'mytest' ])
			}

			'/tmp/reports/r'(){
				group( 'myVar2WithFunction' )
				counter  ( 'trafficAsImpressionsWith(int)SUM' )
			}
		}

		assertEquals(20, builder.parallel)
		def projections = builder.projections

		assertNotNull(projections)
		assertEquals(2, projections.size())

		def projection = projections[0]

		def load = builder.load
		assertNotNull(load)

		assertEquals('/logs/type', load.path)
		assertEquals('com.Loader', load.function)

		def store = projection.store
		assertNotNull(store)
		assertEquals('/tmp/reports/myprojection', store.path)
		assertEquals('com.analytics.mystore.Store(\'ads\')', store.function)
		assertEquals('mytest', store.name)
		
		def groups = projection.groups

		assertEquals(2, groups.size())
		assertEquals('myVar', groups[0].column)
		assertEquals('var1', groups[0].alias)
		assertEquals('', groups[0].function)
		assertEquals('myVar2', groups[1].column)
		assertEquals('myVar2', groups[1].alias)
		assertEquals('Function', groups[1].function)

		def counters = projection.counters

		assertEquals(1, counters.size())
		assertEquals("traffic", counters[0].column)
		assertEquals("Impressions", counters[0].alias)

		def filters = projection.filters
		assertEquals(2, filters.size())
		assertEquals('traffic > 0 and 1 = 1', filters[0].expression)
		assertEquals('traffic == 1', filters[1].expression)

		def orders = projection.orders
		assertEquals(1, orders.size())
		assertEquals('traffic DESC', orders[0].expression)

		println builder
	}
}
