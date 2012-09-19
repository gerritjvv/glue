package org.glue.unit.om.impl

import groovy.util.ConfigObject

import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueModuleFactoryProvider
import org.glue.unit.om.impl.spring.GlueModulePostProcessor
import org.glue.unit.script.ScriptClassCache;
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.GenericBeanDefinition

/**
 * 
 * Integrates the GlueModule(s) GlueModuleFactory with Spring.<br/>
 * All GlueModule(s) instantiated with this provider can use annotations to inject dependencies.<br/>
 */
@Typed(TypePolicy.MIXED)
class SpringGlueModuleFactoryProvider implements GlueModuleFactoryProvider {

	private static final Logger LOG = Logger.getLogger(SpringGlueModuleFactoryProvider.class)

	DefaultListableBeanFactory beanFactory

	Collection<String> moduleNames = []

	GlueModulePostProcessor postProcessor = new GlueModulePostProcessor()

	public SpringGlueModuleFactoryProvider(String config, DefaultListableBeanFactory beanFactory){

		//we register the GlueModulePostProcessor here so that
		//any GlueModule instances are properly initialised with
		//the correct ConfigObject instance.
		beanFactory.addBeanPostProcessor(postProcessor)

		ConfigObject configObject

		if(config){
			try{
				configObject = ScriptClassCache.getDefaultInstance().parse(config)
			}catch(GroovyRuntimeException exp){
				//error in module config
				throw new ModuleConfigurationException("Error in module configuration", null, exp)
			}
		}

		init(configObject, beanFactory);
	}


	public SpringGlueModuleFactoryProvider(ConfigObject configObject,
	DefaultListableBeanFactory beanFactory) {
		super();
		init(configObject, beanFactory)
	}

	/**
	 * Initialises the GlueModule(s) and adds them to the beanFactory.
	 * @param configObject
	 * @param beanFactory
	 */
	private void init(ConfigObject configObject, DefaultListableBeanFactory beanFactory){
		this.beanFactory = beanFactory;

		configObject?.each { String name, ConfigObject moduleConfig ->
			addToBeanFactory(beanFactory, name, moduleConfig)
		}
	}


	/**
	 * Returns a new instance of GlueModuleFactory, all GlueModules are added from the spring bean factory.<br/>
	 * The bean factory will take care of when to instantiate the singleton and non singleton beans.
	 */
	GlueModuleFactory get(GlueContext context){


		GlueModuleFactoryImpl moduleFactory = new GlueModuleFactoryImpl()

		moduleNames?.each { String name ->

			//add each GlueModule bean to the module factory
			//spring will take care of the instantiation of non singleton beans

			GlueModule module = GlueModuleProxy.createProxy(beanFactory.getBean(name, GlueModule.class), context)

			moduleFactory << [
				name,
				module,
				beanFactory.getBeanDefinition(name).scope == BeanDefinition.SCOPE_SINGLETON
			]

		}

		return moduleFactory
	}

	/**
	 * Ads the module to the running spring context
	 * @param name
	 * @param moduleConfig
	 */
	public void addModule(String name, ConfigObject moduleConfig){
		addToBeanFactory(beanFactory, name, moduleConfig)

		//do this to initialize any singleton beans
		boolean isSingleton = (moduleConfig.isSingleton) ? Boolean.valueOf(moduleConfig.isSingleton) : true
		if(isSingleton){
			def bean = beanFactory.getBean(name)
			println bean
		}
	}

	/**
	 * Create a AnnotatedGenericBeanDefinition instance and add it to the beanFactory provided.<br/>
	 * If the module name already exists in the beanFactory then a ModuleConfigurationException is thrown.<br/>
	 * @param beanFactory
	 * @param name
	 * @param moduleConfig
	 */
	private void addToBeanFactory(DefaultListableBeanFactory beanFactory, String name, ConfigObject moduleConfig){


		if(beanFactory.containsBean(name)){
			throw new ModuleConfigurationException("Another internal object is already defined with name:$name", moduleConfig)
		}

		beanFactory.registerBeanDefinition(
				name,
				createModuleBeanDefinition(name, moduleConfig, beanFactory)
				)


		moduleNames << name.toString()
	}

	/**
	 * Creates a AnnotatedGenericBeanDefinition from the moduleconfig.<br/>
	 * 
	 * @param name
	 * @param moduleConfig
	 * @return
	 */
	private GenericBeanDefinition createModuleBeanDefinition(String name, ConfigObject moduleConfig, DefaultListableBeanFactory beanFactory) throws ModuleConfigurationException{

		String moduleClassName = moduleConfig?.className

		if(!moduleClassName || moduleClassName == '{}'){
			throw new ModuleConfigurationException("Property className must be defined for module $name", moduleConfig)
		}

		Class<GlueModule> moduleClass

		try{
			moduleClass = Thread.currentThread().getContextClassLoader().loadClass(moduleClassName)
		}catch(Throwable exp){
			try{
				moduleClass = getClass().getClassLoader().findClass(moduleClassName)
			}catch(Throwable cnf){
				throw new ModuleConfigurationException("Class $moduleClassName was not found for module $name", moduleConfig, cnf)
			}
		}

		if(moduleClass.isAssignableFrom(GlueModule.class)){
			throw new ModuleConfigurationException("The class $moduleClass must implement the interface ${GlueModule.class} for module $name", moduleConfig)
		}

		//to be true the variable must equal "TRUE" or "true" or not defined, and other value including "false", "FALSE" is false
		boolean isSingleton = (moduleConfig.isSingleton) ? Boolean.valueOf(moduleConfig.isSingleton) : true

		LOG.debug "Creating module $moduleClassName singleton:$isSingleton"

		AnnotatedGenericBeanDefinition beanDef = new AnnotatedGenericBeanDefinition(moduleClass)

		beanDef.setScope( (isSingleton)? "singleton" : "prototype" )

		//we add this beans config to the GlueModulePostProcessor
		//the post processor will be called after the GlueModule bean
		//is instantiated and it in turn will call the init method
		//on the GlueModule passing the moduleConfig to it
		postProcessor.addConfig name, moduleConfig.config

		return beanDef
	}
}
