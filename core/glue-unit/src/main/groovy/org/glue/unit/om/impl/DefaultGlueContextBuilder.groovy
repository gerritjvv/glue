package org.glue.unit.om.impl

import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueContextBuilder
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.GlueUnit
import org.glue.unit.status.GlueUnitStatusManager

/**
 *
 * Provides a single place where the GlueContext is built.
 *
 */
@Typed
class DefaultGlueContextBuilder implements GlueContextBuilder{

	GlueModuleFactoryProvider moduleFactoryProvider
	GlueUnitStatusManager statusManager
	GlueExecLoggerProvider loggerProvider
	
	public DefaultGlueContextBuilder(){
	}
	
	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider){
		this(moduleFactoryProvider, null)
	}
	
	
	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider, GlueUnitStatusManager statusManager){
		this.moduleFactoryProvider = moduleFactoryProvider
		this.statusManager = statusManager
	}
	
	public DefaultGlueContextBuilder(GlueModuleFactoryProvider moduleFactoryProvider, GlueUnitStatusManager statusManager,
		GlueExecLoggerProvider loggerProvider){
		this.moduleFactoryProvider = moduleFactoryProvider
		this.statusManager = statusManager
		this.loggerProvider = loggerProvider
	}
	
	GlueContext build(String unitId, GlueUnit unit, Map<String,String> params){
		
		//with each GlueContext we create a unique instance of the moduleFactory
		//unique here means that all non singleton modules will be instantiated and
		//configured a new for this GlueContextInstance
		GlueContextImpl context = new GlueContextImpl()
		context.unitId = unitId
		context.unit = unit
		context.args = params
		context.statusManager = statusManager 
		context.moduleFactory = moduleFactoryProvider?.get(context)
		context.logger = loggerProvider?.get(unitId)
		
		return context
	}
	
}
