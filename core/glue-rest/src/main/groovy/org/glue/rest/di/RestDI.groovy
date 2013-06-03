package org.glue.rest.di

import org.glue.rest.Server
import org.glue.rest.config.LauncherConfig
import org.glue.rest.resources.ModulesResource
import org.glue.rest.resources.ProcessStatusResource
import org.glue.rest.resources.ShutdownResource
import org.glue.rest.resources.StatusResource
import org.glue.rest.resources.StatusRunningResource
import org.glue.rest.resources.SubmitUnitResource
import org.glue.rest.resources.TerminateResource
import org.glue.rest.resources.UnitStatusResource
import org.glue.rest.resources.WorkflowCheckResource
import org.glue.rest.resources.WorkflowHistoryResource
import org.glue.unit.exec.GlueExecutor
import org.glue.unit.log.GlueExecLoggerProvider
import org.glue.unit.repo.GlueUnitRepository
import org.glue.unit.status.GlueUnitStatusManager
import org.restlet.Component
import org.restlet.data.Protocol
import org.restlet.ext.spring.SpringBeanRouter
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

/**
 *
 * Contains the DI configuration for the Restlet components
 *
 */
class RestDI {
	
	
	@Autowired(required = true)
	BeanFactory beanFactory
	
	@Autowired(required = true)
	LauncherConfig config
	
	/**
	 * Create a Rest Component. To Use call start
	 * @return Component
	 */
	@Bean
	Component getRestServerComponent(){
		
		Server server = beanFactory.getBean(Server.class)
		
		Component component = new Component()
		component.getServers().add ( Protocol.HTTP, config.restServerPort);
		component.getDefaultHost().attach( server );
		
		return component
	}
	
	/**
	 * Main entry to the rest components.
	 * Configures the paths to the resources
	 * @return Server
	 */
	@Bean
	Server server(){
		SpringBeanRouter router = new SpringBeanRouter()
		
		// Defines a route for the resource "list of items"
		router.attachResource("/submit", 'submitUnitResource', beanFactory);
		router.attachResource("/stop", 'shutdownResource', beanFactory);
		// Defines a route for the resource "item"
		router.attachResource("/status", 'statusResource', beanFactory);
		router.attachResource("/status/{unitId}", 'unitStatusResource', beanFactory);
		router.attachResource("/kill/{unitId}", 'terminateResource', beanFactory);
		router.attachResource("/status/{unitId}/{processName}", 'processStatusResource', beanFactory);
//		WorkflowHistoryResource
		
		router.attachResource("/history/{workflowName}", 'workflowHistoryResource', beanFactory);
		router.attachResource("/check/{workflowName}", 'workflowCheckResource', beanFactory);
		
		router.attachResource("/modules", 'modulesResource', beanFactory);
		router.attachResource("/running", 'statusRunningResource', beanFactory);
		
		new Server(router)
		
	}
	
	
	
	
	//----------------- Rest Resources --------------------------//
	@Bean
	@Scope('prototype')
	ModulesResource modulesResource(){
		new ModulesResource(beanFactory.getBean(GlueExecutor.class),
		beanFactory.getBean("moduleFactoryProvider"))
	}
	@Bean
	@Scope('prototype')
	SubmitUnitResource submitUnitResource(){
		new SubmitUnitResource(beanFactory.getBean(GlueExecutor.class))
	}
	@Bean
	@Scope('prototype')
	ShutdownResource shutdownResource(){
		new ShutdownResource(beanFactory.getBean(GlueExecutor.class))
	}
	@Bean
	@Scope('prototype')
	StatusResource statusResource(){
		new StatusResource(beanFactory.getBean(GlueUnitStatusManager.class),
		beanFactory.getBean(GlueUnitRepository.class))
	}
	
	@Bean
	@Scope('prototype')
	TerminateResource terminateResource(){
		new TerminateResource(beanFactory.getBean(GlueExecutor.class))
	}
	//WorkflowCheckResource
	@Bean
	@Scope('prototype')
	WorkflowHistoryResource workflowHistoryResource(){
		new WorkflowHistoryResource(
		beanFactory.getBean(GlueUnitStatusManager.class),
		)
	}
	
	@Bean
	@Scope('prototype')
	WorkflowCheckResource workflowCheckResource(){
		new WorkflowCheckResource(
		beanFactory.getBean(GlueUnitStatusManager.class),
		beanFactory.getBean(GlueUnitRepository.class)
		)
	}
	
	@Bean
	@Scope('prototype')
	UnitStatusResource unitStatusResource(){
		new UnitStatusResource(
		beanFactory.getBean(GlueExecutor.class),
		beanFactory.getBean(GlueUnitStatusManager.class),
		beanFactory.getBean(GlueUnitRepository.class)
		)
	}
	@Bean
	@Scope('prototype')
	ProcessStatusResource processStatusResource(){
		
		new ProcessStatusResource(
		beanFactory.getBean(GlueUnitStatusManager.class),
		beanFactory.getBean(GlueUnitRepository.class),
		beanFactory.getBean("moduleFactoryProvider"),
		beanFactory.getBean(GlueExecLoggerProvider.class)
		)
	}
	
	@Bean
	@Scope('prototype')
	StatusRunningResource statusRunningResource(){
		new StatusRunningResource(
			beanFactory.getBean(GlueExecutor.class)
		)
	}
	
	
}
