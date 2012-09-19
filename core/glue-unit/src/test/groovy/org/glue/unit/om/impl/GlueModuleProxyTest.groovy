package org.glue.unit.om.impl;

import static org.junit.Assert.*

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.junit.Test

/**
 * 
 * Tests that the GlueModuleProxy correctly intercepts method calls to the GlueModule
 *
 */
class GlueModuleProxyTest {

	/**
	 * Shows how to use the proxy with no arguments
	 */
	@Test
	public void testCallNoArgument(){

		String unitId = '123'
		MockGlueModule module = new MockGlueModule()
		String name = module.name
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals(name, moduleProxy.getName())
		//assert twice to check cache
		assertEquals(name, moduleProxy.getName())
	}


	/**
	 * Shows how to use the proxy with the arguments as a collection
	 */
	@Test
	public void testCallWithCollection(){

		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals(unitId, moduleProxy.getUnitId(['name']))
		//assert twice to check cache
		assertEquals(unitId, moduleProxy.getUnitId(['name']))
	}

	/**
	 * Tests that a method is called that have an array as argument.
	 */
	@Test
	public void testCallWithArrayOnlyAsArgument(){

		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])

		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals('name', moduleProxy.getFirstItemInArray(['name']as Object[]))
		//assert twice to check cache
		assertEquals('name', moduleProxy.getFirstItemInArray(['name']as Object[]))
	}

	/**
	 * Tests that a method is called that have a Collection as argument.
	 */
	@Test
	public void testCallWithCollectionOnlyAsArgument(){

		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])

		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals('name', moduleProxy.getFirstItemInCollection(['name']))
		//assert twice to check cache
		assertEquals('name', moduleProxy.getFirstItemInCollection(['name']))
	}


	/**
	 * Tests that a method is called that have glue context, name, and an array as argument.
	 */
	@Test
	public void testCallWithGlueContextAndArrayOnlyAsArgument(){
		println "hi"
		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])

		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals('name', moduleProxy.getFirstItemInArrayNameWithContext(context, 'hi', ['name']as Object[]))
		//assert twice to check cache
		assertEquals('name', moduleProxy.getFirstItemInArrayNameWithContext(context, 'hi', ['name']as Object[]))
	}

	/**
	 * Shows how to use the proxy
	 */
	@Test
	public void testWithProxy(){

		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModule moduleProxy = GlueModuleProxy.createProxy(module, context)

		assertEquals(unitId, moduleProxy.getUnitId('name'))
		//assert twice to check cache
		assertEquals(unitId, moduleProxy.getUnitId('name'))
	}

	/**
	 * Test that if we call a method without the context and it does contain it in the method definition
	 * that this method will be correctly called.
	 */
	@Test
	public void testGlueModuleCallWithoutContext(){

		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModuleProxy proxy = new GlueModuleProxy(
				module,
				context
				)

		assertEquals(unitId, proxy.getUnitId('name'))
		//call twice to test cache
		assertEquals(unitId, proxy.getUnitId('name'))

	}

	/**
	 * Test that a method without the GlueContext is still called.
	 */
	@Test
	public void testNoContextMethod(){
		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModuleProxy proxy = new GlueModuleProxy(
				module,
				context
				)

		//tests none context
		assertEquals('name', proxy.getName('name'))
		//cat twice to test cache
		assertEquals('name', proxy.getName('name'))
	}

	/**
	 * Test that a method without the GlueContext is still called even if the arguments is passed
	 * as a collection.
	 */
	@Test
	public void testNoContextArgsAsCollectionMethod(){
		String unitId = '123'
		GlueModule module = new MockGlueModule()
		GlueContext context = new DefaultGlueContextBuilder().build(unitId, null, [:])


		GlueModuleProxy proxy = new GlueModuleProxy(
				module,
				context
				)

		//tests none context
		assertEquals('name', proxy.getName(['name']))
		//cat twice to test cache
		assertEquals('name', proxy.getName(['name']))
	}
}
