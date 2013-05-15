package org.glue.modules.hadoop.cascalog


import javax.script.ScriptEngineManager
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import clojure.lang.*

/**
 * 
 * Provides Cascalog support from Glue workflows.
 * 
 */
class CascalogModule implements GlueModule{

	def defaultConfiguration
	def hdfsConfigurations = [:]
	
	ScriptEngineManager manager = new ScriptEngineManager();
	
	
	def getConf(String clusterName){
		return (clusterName)? hdfsConfigurations[clusterName] : hdfsConfigurations[defaultConfiguration]
	}
	
	public void exec(String cluster=null, String script){

		def file = getConf(cluster)
		def script2 = """
			(require  '[cascalog.playground])
			(cascalog.playground/bootstrap)

			;this code will set the job configuration
			(let [jobProperties (with-open [rdr (clojure.java.io/reader "$file") ]
					(let [p (java.util.Properties.)]
					(.load p rdr) p
					))]

				(cascalog.conf/set-job-conf! (into {} jobProperties)))
				;we add the person and age vars to allow easy testing
				(def person
				  [
				   ;; [person]
				   ["alice"]
				   ["bob"]
				   ["chris"]
				   ["david"]
				   ["emily"]
				   ["george"]
				   ["gary"]
				   ["harold"]
				   ["kumar"]
				   ["luanne"]
				   ])
				
				(def age
				  [
				   ;; [person age]
				   ["alice" 28]
				   ["bob" 33]
				   ["chris" 40]
				   ["david" 25]
				   ["emily" 25]
				   ["george" 31]
				   ["gary" 28]
				   ["kumar" 27]
				   ["luanne" 36]
				   ])
			$script
		"""
//		Compiler.load(new java.io.StringReader(script2))
		
		manager.getEngineByName("Clojure").eval(script2)
		
	}

	@Override
	public void init(ConfigObject config) {
		if(!config.clusters) {
			println "Can't find any clusters in config!"
		}

		config.clusters.each { String key, ConfigObject c ->

			print "loading cluster $key"
			if(c.isDefault) {
				defaultConfiguration=key;
			}
			
			hdfsConfigurations[key]=c.jobconf
			println "Loaded $key as ${c.jobconf}"

			if(!c.jobconf){
				throw new ModuleConfigurationException("No property jobconf defined for CascalogModule in $key")
			}
		}

		if(defaultConfiguration == null){
			throw new ModuleConfigurationException("No default configuration was specified for the cascalog module configuration $config")
		}

	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
		
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
		
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true;
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,
			Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessKill(GlueProcess process, GlueContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(String unitId, ConfigObject config) {
		// TODO Auto-generated method stub
		
	}
	
}
