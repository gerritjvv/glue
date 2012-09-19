package org.glue.unit.om;

import groovy.util.ConfigObject;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * 
 * Each GlueUnit has a context during its execution.
 *
 */
@Typed(TypePolicy.MIXED)
public interface GlueModule {
	
	void init(ConfigObject config);
	void onUnitStart(GlueUnit unit, GlueContext context);
	void onUnitFinish(GlueUnit unit, GlueContext context);
	void onUnitFail(GlueUnit unit, GlueContext context);

	Boolean canProcessRun(GlueProcess process, GlueContext context);
	void onProcessStart(GlueProcess process,GlueContext context);
	void onProcessFinish(GlueProcess process, GlueContext context);
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t);
	
	void onProcessKill(GlueProcess process, GlueContext context);
	
	String getName();
	
	void destroy()
	
	public Map getInfo();
	
	void configure(String unitId, ConfigObject config);
}
