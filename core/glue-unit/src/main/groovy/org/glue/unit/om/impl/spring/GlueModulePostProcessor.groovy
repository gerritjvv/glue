package org.glue.unit.om.impl.spring

import org.springframework.beans.BeansException;
import org.glue.unit.om.GlueModule
import org.apache.log4j.Logger
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor

/**
 *
 * We use this post processor to initialise GlueModule instances with the property ConfigObject.<br/>
 * This one instance is meant to contain all ConfigObject(s) for all the GlueModule(s)
 * 
 */
@Typed
class GlueModulePostProcessor implements BeanPostProcessor{

	private static final Logger LOG = Logger.getLogger(GlueModulePostProcessor.class)

	Map<String, ConfigObject> configs = [:]

	public GlueModulePostProcessor(){
	}

	public void addConfig(String name, ConfigObject config){
		configs[name] = config
	}

	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException{
		//we need to return the bean here. Or else spring will see the bean as null
		return bean
	}

	/**
	 * If the bean is an instance of GlueModule we call the init method with the corresponding<br/> 
	 * ConfigObject in the configs map.
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException{

		println "postProcessAfterInitialization: $bean $beanName"
		if(GlueModule.class.isAssignableFrom(bean.class)){
			try{
				((GlueModule)bean).init( configs[beanName] )
			}catch(Throwable t){
				println "Error configuring module $beanName"
				t.printStackTrace()
				throw t 
			}
		}

		return bean
	}
}
