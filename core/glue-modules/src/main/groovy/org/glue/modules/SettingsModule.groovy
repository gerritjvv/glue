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
    private String envname = null;
    private Map manualSettings = [:];
    //private Map linkedSettings = [:];
    
    private Map<String, Map> _sets = new HashMap<String, Map>();
    
    
    // settings.environment
    public String getEnvironment()
    {
        return envname;
    }

    public Map propertyMissing(String name)
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
            
            println "getSettings '$name'";
            // If you get a StackOverflowError with a slew of getSettings calls,
            // look for that name in this function and remove it, it doesn't exist.
            
            def binding = [
                println:{ ln -> println(ln) },
                current:m,
                glueContext:ctx,
                capabilities:['glue',] as HashSet<String>,
            ];
            
            //mergeSettingsMap(m, linkedSettings);
            
            SettingsFileType sft = new SettingsFileType();
            
            // global:
            for(File f : (new File(settingsdir + "/linked/global")).listFiles(sft))
            {
                println "Loading linked settings from '$f'";
                def co = loadSingleFile(envname, f, binding);
                mergeSettingsMap(m, co);
            }
            
            // env:
            for(File f : (new File(settingsdir + "/linked/" + envname)).listFiles(sft))
            {
                println "Loading linked settings from '$f'";
                def co = loadSingleFile(envname, f, binding);
                mergeSettingsMap(m, co);
            }
            
            // name:
            if(true)
            {
                File f = new File(settingsdir + "/" + name + SettingsFileType.DOT_EXT);
                if(f.exists())
                {
                    println "Loading settings from '$f'";
                    def co = loadSingleFile(envname, f, binding);
                    mergeSettingsMap(m, co);
                }
            }
            
            // name env:
            if(true)
            {
                File f = new File(settingsdir + "/" + envname + "/" + name + SettingsFileType.DOT_EXT);
                if(f.exists())
                {
                    println "Loading settings from '$f'";
                    def co = loadSingleFile(envname, f, binding);
                    mergeSettingsMap(m, co);
                }
            }
            
            mergeSettingsMap(m, manualSettings);
        }
        _sets[name] = m;
        return m;
    }
    
    
    protected ConfigObject loadSingleFile(String envname, File f, Map binding)
    {
        try
        {
            URL url = f.toURI().toURL();
            //def cs = new ConfigSlurper(envname);
            def cs = new MyConfigSlurper(envname);
            cs.setBinding(binding);
            return cs.parse(url);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Exception while loading settings from '$f': $e", e);
        }
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
                /*if(v instanceof Cloneable)
                {
                    settings[k] = v.clone();
                }
                else*/ if(true)
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
        this.envname = config.environment
        
        if(!config.settingsdir) {
            throw new ModuleConfigurationException("settingsdir not set in config section")
        }
        this.settingsdir = config.settingsdir
        (new File(settingsdir + "/linked/global")).mkdirs()
        (new File(settingsdir + "/linked/" + envname)).mkdirs()
        (new File(settingsdir + "/" + envname)).mkdirs()
        
        println "Settings directory '$settingsdir' for environment '$envname'"
        
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
    public void onProcessFail(GlueProcess process, GlueContext context,    Throwable t) {
    }

    @Override
    public void onProcessFinish(GlueProcess process, GlueContext context) {
    }

    @Override
    public void onProcessStart(GlueProcess process, GlueContext context) {
    }
    
    
    // Fix issue where foo=0; bar="a$foo" makes foo an empty map and bar "a[:]"
    private static class MyConfigSlurper
    {
        private static final ENV_METHOD = "environments"
        static final ENV_SETTINGS = '__env_settings__'
        GroovyClassLoader classLoader = new GroovyClassLoader()
        String environment
        private envMode = false
        private Map bindingVars
        
        MyConfigSlurper() { }
        
        MyConfigSlurper(String env) {
            this.environment = env
        }
        
        void setBinding(Map vars) {
            this.bindingVars = vars
        }
        
        ConfigObject parse(Properties properties) {   
            ConfigObject config = new ConfigObject()
            for(key in properties.keySet()) {
                def tokens = key.split(/\./)
                
                def current = config
                def last
                def lastToken
                def foundBase = false
                for(token in tokens) {
                    if (foundBase) {
                        // handle not properly nested tokens by ignoring
                        // hierarchy below this point
                        lastToken += "." + token
                        current = last
                    } else {
                        last = current
                        lastToken = token
                        current = current."${token}"
                        if(!(current instanceof ConfigObject)) foundBase = true
                    }
                }
    
                if(current instanceof ConfigObject) {
                    if(last[lastToken]) {
                        def flattened = last.flatten()
                        last.clear()
                        flattened.each { k2, v2 -> last[k2] = v2 }
                        last[lastToken] = properties.get(key)
                    }
                    else {                    
                        last[lastToken] = properties.get(key)
                    }
                }
                current = config
            }
            return config
        }
        
        ConfigObject parse(String script) {
            return parse(classLoader.parseClass(script))
        }
        
        ConfigObject parse(Class scriptClass) {
            return parse(scriptClass.newInstance())
        }
        
        ConfigObject parse(Script script) {
            return parse(script, null)
        }
        
        ConfigObject parse(URL scriptLocation) {
            return parse(classLoader.parseClass(scriptLocation.text).newInstance(), scriptLocation)
        }
        
        ConfigObject parse(Script script, URL location) {
            def config = location ? new ConfigObject(location) : new ConfigObject()
            GroovySystem.metaClassRegistry.removeMetaClass(script.class)
            def mc = script.class.metaClass
            def prefix = ""
            LinkedList stack = new LinkedList()
            stack << [config:config,scope:[:]]
            def pushStack = { co ->
                stack << [config:co,scope:stack.last.scope.clone()]
            }
            def assignName = { name, co ->
                def current = stack.last
                current.config[name] = co
                current.scope[name] = co
            }
            def getPropertyClosure = { String name ->
                def current = stack.last
                def result
                if(current.config.get(name) != null) {
                    result = current.config.get(name)
                } else if(current.scope[name] != null) {
                    result = current.scope[name]
                } else {
                    try {
                        result = InvokerHelper.getProperty(this, name);
                    } catch (GroovyRuntimeException e) {
                        result = new ConfigObject()
                        assignName.call(name,result)
                    }
                }
                result
            }
            mc.getProperty = getPropertyClosure
            mc.invokeMethod = { String name, args ->
                def result
                if(args.length == 1 && args[0] instanceof Closure) {
                    if(name == ENV_METHOD) {
                        try {
                            envMode = true
                            args[0].call()
                        } finally {
                            envMode = false
                        }
                    } else if (envMode) {
                        if(name == environment) {
                            def co = new ConfigObject()
                            config[ENV_SETTINGS] = co
    
                            pushStack.call(co)
                            try {
                                envMode = false
                                args[0].call()
                            } finally {
                                envMode = true
                            }
                            stack.pop()
                        }
                    } else {
                        def co
                        if (stack.last.config.get(name) instanceof ConfigObject) {
                            co = stack.last.config.get(name)
                        } else {
                            co = new ConfigObject()
                        }
    
                        assignName.call(name, co)
                        pushStack.call(co)
                        args[0].call()
                        stack.pop()
                    }
                } else if (args.length == 2 && args[1] instanceof Closure) {
                    try {
                    prefix = name +'.'
                    assignName.call(name, args[0])
                    args[1].call()
                    }  finally { prefix = "" }
                } else {
                    MetaMethod mm = mc.getMetaMethod(name, args)
                    if(mm != null) {
                        result = mm.invoke(delegate, args)
                    } else {
                        throw new MissingMethodException(name, getClass(), args)
                    }
                }
                result
            }
            script.metaClass = mc
    
            def setProperty = { String name, value ->
                assignName.call(prefix+name, value)
            }                
            def binding = new ConfigBinding(setProperty)
            if(this.bindingVars != null) {
                binding.getVariables().putAll(this.bindingVars)
            }
            script.binding = binding
    
    
            script.run()
    
            def envSettings = config.remove(ENV_SETTINGS)
            if(envSettings != null) {
                config.merge(envSettings)
            }
    
            return config
        }
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







