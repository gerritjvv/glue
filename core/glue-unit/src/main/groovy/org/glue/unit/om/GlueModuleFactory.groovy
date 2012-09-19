package org.glue.unit.om;

import java.util.Map

/**
 *
 * The GlueModuleFactory is responsible for creating and managing all module instances.<br/>
 * Two implementations are provided:<br/>
 * <ul>
 *  <li>SpingGlueModuleFactoryImpl</li>
 *  <li>GlueModuleFactoryImpl</li>
 * </ul>
 * <p/>
 * <b>SpingGlueModuleFactoryImpl<b/><br/>
 * Hands the instance management and creation task to spring.<br/>
 * Bean definitions are dynamically loaded into the provided spring bean factory.<br/>
 * <p/>
 * <b>GlueModuleFactoryImpl<b/><br/>
 * Uses an internal map to manage instances.<br/>
 * This implementation should be used for testing</br>
 * 
 *
 */
@Typed
public interface GlueModuleFactory {
	
	Map<String,GlueModule> getAvailableModules()
	GlueModule getModule(String moduleName)

	void onUnitFinish(GlueUnit unit, GlueContext context)
	void onUnitFail(GlueUnit unit, GlueContext context)
	void onUnitStart(GlueUnit unit, GlueContext context)

	void onProcessFinish(GlueProcess process, GlueContext context)
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t)
	void onProcessStart(GlueProcess process, GlueContext context)

	
	void destroy()
	
	void leftShift(String name, GlueModule module)
	
	void addModule(String name, GlueModule module)
	
	
}
