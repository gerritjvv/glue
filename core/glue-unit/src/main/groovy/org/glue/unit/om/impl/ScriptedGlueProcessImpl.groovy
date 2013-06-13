package org.glue.unit.om.impl

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

import org.apache.log4j.Logger
import org.glue.unit.om.GlueContext
import org.glue.unit.om.impl.jython.PythonContextAdaptor
import org.python.core.Py
import org.python.core.PySystemState


/**
 * 
 * Use the java script interface to run any script jython, jruby, etc as a process.
 *
 */
@Typed
class ScriptedGlueProcessImpl extends GlueProcessImpl{

	private static final Logger log = Logger.getLogger(ScriptedGlueProcessImpl.class)

	static final ScriptEngineManager factory = new ScriptEngineManager(Thread.currentThread().getContextClassLoader());

	/**
	 * 
	 * @param name
	 * @param config The config object must have the format config.script_[language]
	 *  e.g. config.script_jython= """ jython code here """
	 */
	public ScriptedGlueProcessImpl(String name, ConfigObject config){
		super(name, loadScript(config))
	}
	@Typed(TypePolicy.MIXED)
	def static ConfigObject loadScript(ConfigObject config){

		//find the first item with script_[any]
		def script = config.find { it.toString().startsWith('script_') }

		if(script == null){
			throw new RuntimeException("No script_ object found in " + config)
		}

		def splits = script.key.toString().split('_')
		if(splits.length != 2){
			throw new RuntimeException(script + " must have format scripts_language in " + config)
		}
		def lang = splits[1]

		if(lang == "jython"){
			//Initialize the Python Interpreter with the custom PythonContextAdaptor.
			//The problem is that all groovy interfaces when the method getDeclaredClasses is called
			//return a strange [Name]$1 class, which does not exist and cause a ClassNotFoundException in Java.
			//The PythonContextAdaptor use a work-around
			def adaptor = new PythonContextAdaptor()
			PySystemState.initialize(null, null, [""], Thread.currentThread().getContextClassLoader(), 
				adaptor)
			Py.setAdapter(adaptor)
			
		}else if(lang == "jruby"){
			lang = "ruby"
		} else if (lang == "clj") {
			lang = "Clojure"
		}

		ScriptEngine engine = factory.getEngineByName(lang);

		if(engine == null)
			engine = factory.getEngineByExtension(lang);

		if(engine == null){
			throw new RuntimeException("No libraries was found for the language " + lang + " specified in " + config)
		}


		log.info("overwriting tasks with closure for language " + lang)


		config.tasks = { GlueContext ctx ->
			
			GlueContext ctx1 = DefaultGlueContextBuilder.buildStaticGlueContext(ctx)
			log.info("setting memory to: context|ctx => GlueContext : ")
			def bindings = engine.createBindings()
			bindings.put("context", ctx1)
			bindings.put("ctx", ctx1)
			
			log.info("binding.context: " + ctx + " class: " + ctx1.getClass())
			engine.eval(
				new String(script.value.decodeBase64())
				, bindings)
		}

		return config
	}

	@Override
	public String getName() {
		super.getName()
	}

	@Override
	public Closure getTask() {
		super.getTask()
	}

	@Override
	public Closure getSuccess() {
		super.getSuccess()
	}

	@Override
	public Closure getError() {
		super.getError()
	}

	@Override
	public String getDescription() {
		super.getDescription()
	}

	@Override
	public Set<String> getDependencies() {
		super.getDependencies()
	}

}
