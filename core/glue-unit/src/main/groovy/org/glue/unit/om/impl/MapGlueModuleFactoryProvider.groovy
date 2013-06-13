package org.glue.unit.om.impl

import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueModuleFactoryProvider

/**
 *
 * Used for testing. Will add all modules to the GlueModuleFactory.<br/>
 *
 *
 */
@Typed(TypePolicy.MIXED)
class MapGlueModuleFactoryProvider implements GlueModuleFactoryProvider{

	Map<String, GlueModule> singletonMap = [:]

	Map<String, ConfigObject> prototypeConfigs = [:]

	public MapGlueModuleFactoryProvider(){
		
	}
	
	public MapGlueModuleFactoryProvider(ConfigObject configObject){

		//load singletons
		configObject?.each { String name, ConfigObject config ->

			if(Boolean.valueOf(config?.isSingleton)){

				singletonMap[name] = createGlueModule(name, config)
				
			}else{
				prototypeConfigs[name] = config
			}
		}
	}
	
	void removeModule(String name){
		
		singletonMap.remove name
		prototypeConfigs.remove name
			
	}
	
	public void addModule(String name, GlueModule module){
		singletonMap[name] = module
	}
	
	public void addModule(String name, ConfigObject config, boolean isSingleton){
		if(isSingleton){
			singletonMap[name] = createGlueModule(name, config)
		}else{
			prototypeConfigs[name] = config
		}
	}
	
	public void addSingletonModule(String name, GlueModule module){
		singletonMap[name] = module
	}
	
	@Typed(TypePolicy.MIXED)
	GlueModuleFactory get(GlueContext context){
		
		GlueModuleFactory factory = new GlueModuleFactoryImpl()
		
		singletonMap.each { String name, GlueModule module ->
			factory << [name, GlueModuleProxy.createProxy(module, context), true]
		}
		
		prototypeConfigs.each { String name, ConfigObject moduleConfig ->
			factory << [name,
				GlueModuleProxy.createProxy(
				 createGlueModule(name, moduleConfig), context)
				, false]
		}
		
		return factory
	}

	/**
	 * Creates a GlueModule
	 * @param name
	 * @param moduleConfig
	 * @return
	 */
	GlueModule createGlueModule(String name, ConfigObject moduleConfig){

		String moduleClassName = moduleConfig?.className

		if(!moduleClassName || moduleClassName == '{}'){
			throw new ModuleConfigurationException("Property className must be defined for module $name", moduleConfig)
		}

		Class<GlueModule> moduleClass

		try{
			moduleClass = Thread.currentThread().getContextClassLoader().loadClass(moduleClassName)
		}catch(ClassNotFoundException exp){
			try{
				moduleClass = getClass().getClassLoader().findClass(moduleClassName)
			}catch(ClassNotFoundException cnf){
				throw new ModuleConfigurationException("Class $moduleClassName was not found for module $name", moduleConfig, cnf)
			}
		}

		if(moduleClass.isAssignableFrom(GlueModule.class)){
			throw new ModuleConfigurationException("The class $moduleClass must implement the interface ${GlueModule.class} for module $name", moduleConfig)
		}

	    GlueModule module = (GlueModule)moduleClass.newInstance()

		module.init moduleConfig.config
		
		return module 
		 
	}
}
