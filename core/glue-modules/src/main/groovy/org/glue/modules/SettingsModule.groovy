package org.glue.modules;

import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit
import org.glue.unit.exceptions.ModuleConfigurationException

import groovy.util.ConfigSlurper

/**
 *
 * 
 */
//@Typed(TypePolicy.DYNAMIC)
class SettingsModule implements GlueModule {


	private ConfigObject config=null;
	private GlueContext ctx = null;

	private String settingsdir = null;
	private String environment = null;
	private Map manualSettings = [:];
	private Map linkedSettings = [:];
	
	private Map<String, Map> _sets = new HashMap<String, Map>();


	public Map getAt(String name)
	{
		return getSettings(name);
	}

	public def getValue(String name, String key)
	{
		return getSettings(name).get(key);
	}

	public Map getSettings(String name)
	{
		Map m = _sets[name];
		if(!m)
		{
			m = [:];
			
			mergeSettingsMap(m, linkedSettings);
			
			// name:
			if(true)
			{
				File f = new File(settingsdir + "/" + name + SettingsFileType.DOT_EXT);
				if(f.exists())
				{
					println "Loading settings from '$f'";
					def co = new ConfigSlurper(environment).parse(f.toURI().toURL());
					mergeSettingsMap(m, co);
				}
			}
			
			// name env:
			if(true)
			{
				File f = new File(settingsdir + "/" + environment + "/" + name + SettingsFileType.DOT_EXT);
				if(f.exists())
				{
					println "Loading settings from '$f'";
					def co = new ConfigSlurper(environment).parse(f.toURI().toURL());
					mergeSettingsMap(m, co);
				}
			}
			
			mergeSettingsMap(m, manualSettings);
			
			_sets[name] = m;
		}
		return m;
	}
	
	
	protected static class SettingsFileType implements FilenameFilter
	{
		final static String DOT_EXT = ".set";
		
		@Override
		boolean accept(File dir, String name)
		{
			return name.endsWith(DOT_EXT);
		}
		
	}
	
	
	protected void mergeSettingsMap(Map settings, Map m)
	{
		m.each { k, v ->
			def sv = settings[k]
			if(!sv || !(sv instanceof Map))
			{
				if(v instanceof Cloneable)
				{
					settings[k] = ((Cloneable)v).clone();
				}
				else
				{
					settings[k] = v;
				}
			}
			else
			{
				mergeSettingsMap(sv, v);
			}
		}
	}


	void destroy(){
		shutdown()
	}

	void onProcessKill(GlueProcess process, GlueContext context){
	}

	@Override
	public Map getInfo() {
		return config;
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true
	}

	public void shutdown(){
	}

	@Override
	public String getName() {
		return 'settings';
	}
	
	
	/**
	 * 
	 * e.g.<br/>
	 * <pre>
	 className='org.glue.modules.SettingsModule'
	 isSingleton=false
	 config {
		environment = "test"
		settingsdir = "/opt/glue/workflow_settings"
	 }
	 * </pre>
	 */
	@Override
	public void init(ConfigObject config) {
		this.config=config;
		
		if(!config.environment) {
			println "environment not set in config, defaulting to \"test\""
		}
		this.environment = config.environment
		
		if(!config.settingsdir) {
			throw new ModuleConfigurationException("settingsdir not set in config section")
		}
		this.settingsdir = config.settingsdir
		(new File(settingsdir + "/linked/global")).mkdirs()
		(new File(settingsdir + "/linked/" + environment)).mkdirs()
		(new File(settingsdir + "/" + environment)).mkdirs()
		
		println "Settings directory '$settingsdir' for environment '$environment'"
		
		SettingsFileType sft = new SettingsFileType();
		
		// global:
		for(File f : (new File(settingsdir + "/linked/global")).listFiles(sft))
		{
			println "Loading init settings from '$f'";
			def co = new ConfigSlurper(environment).parse(f.toURI().toURL());
			mergeSettingsMap(linkedSettings, co);
		}
		
		// env:
		for(File f : (new File(settingsdir + "/linked/" + environment)).listFiles(sft))
		{
			println "Loading init settings from '$f'";
			def co = new ConfigSlurper(environment).parse(f.toURI().toURL());
			mergeSettingsMap(linkedSettings, co);
		}
		
	}


	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
		this.ctx = context
		if(ctx.args instanceof Map && ctx.args.manualSettings instanceof String) {
			ctx.args.manualSettings.split(',').each { kv ->
				int icolon = kv.indexOf(':');
				if(-1 != icolon)
				{
					this.manualSettings[kv.substring(0, icolon)] = kv.substring(icolon + 1);
				}
			}
		}
	}
	
	@Override
	public void configure(String unitId, ConfigObject config) {
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}
	
	
}


/*

settingsdir=/opt/glue/workflow_settings

echo "baz = 'mytestBAZx'
	bat = 'mytestBATx'
	stuff = [
			c : 'mytestCx',
			d : 'mytestDx',
	]" > $settingsdir/mytest.set
echo "bat = 'envmytestBATx'
	stuff = [
			d : 'envmytestDx',
	]" > $settingsdir/test/mytest.set
echo "bar = 'envBARx'
	baz = 'envBAZx'
	bat = 'envBATx'
	stuff = [
			b : 'envBx',
			c : 'envCx',
			d : 'envDx',
	]" > $settingsdir/linked/test/evmt.set
echo "foo = 'globalFOOx'
	bar = 'globalBARx'
	baz = 'globalBAZx'
	bat = 'globalBATx'
	stuff = [
			a : 'globalAx',
			b : 'globalBx',
			c : 'globalCx',
			d : 'globalDx',
	]" > $settingsdir/linked/global/gvmt.set

# from workflow:
println "ctx.settings.mytest={${ctx.settings.mytest}}";
println "ctx.settings.notset={${ctx.settings.notset}}";

# should print:
ctx.settings.mytest={[foo:globalFOOx, bar:envBARx, baz:mytestBAZx, bat:envmytestBATx, stuff:[a:globalAx, b:envBx, c:mytestCx, d:envmytestDx]]}
ctx.settings.notset={[foo:globalFOOx, bar:envBARx, baz:envBAZx, bat:envBATx, stuff:[a:globalAx, b:envBx, c:envCx, d:envDx]]}

*/







