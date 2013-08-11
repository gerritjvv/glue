package org.glue.unit.script

import java.util.concurrent.ConcurrentHashMap

import org.apache.commons.codec.digest.DigestUtils
import org.apache.log4j.Logger




/**
 * Groovy has a grave problem with dynamic class loader and filling up permgen space.<br/>
 * At current date their is no fix for this. So the only reasonable solution is to cache<br/> 
 * Script classes as much as possible.<br/>
 * <p/>
 * 
 * This class provides such a cache. Its static and is thread safe.
 * <p/>
 * How to use:<br/>
 * Always use the getDefaultInstance method.<br/>
 * Instead of doing:<br/>
 * <pre>
 *   new ConfigSlurper().parse("my script")
 * </pre>
 * <br/>
 * Do:<br/>
 * <pre>
 *    new ConfigSlurper().parse(ScriptClassCache.getDefaultInstance().getScript("my script"))
 *    //or
 *    ConfigObject obj = ScriptClassCache.parse("my script")
 * </pre>
 */
@Typed(TypePolicy.MIXED)
class ScriptClassCache {

	private static final Logger LOG = Logger.getLogger(ScriptClassCache)

	private static final ScriptClassCache INSTANCE = new ScriptClassCache();

	Map<String, ScriptCacheItem> checkSumMap = new ConcurrentHashMap<String, ScriptCacheItem>()

	/**
	 * Use in place of new ConfigSlurper().parse
	 * @param text
	 * @return ConfigObject
	 */
	ConfigObject parse(String text){
		println "Test : $text " 
		println "loadScriptText: ${loadScriptText(text)}"
		def c = loadScriptText(text).newInstance()
		println "Class c ${c.class} c= ${c}"
	    new ConfigSlurper().parse(loadScriptText(text).newInstance())
		
	}

	/**
	 * Use in place of new ConfigSlurper().parse
	 * @param file
	 * @return ConfigObject
	 */
	ConfigObject parse(File file){
		new ConfigSlurper().parse(loadScriptFile(file).newInstance())
	}

	/**
	 * Loas a script from the file provided.
	 * @param scriptFile
	 * @return Class<Script>
	 */
	public Class<Script> loadScriptFile(File scriptFile){
		loadScript(scriptFile.absolutePath, calcCheckSum(scriptFile.text), scriptFile)
	}

	/**
	 * Loads a script class from the text provided
	 * @param scriptText
	 * @return
	 */
	public Class<Script> loadScriptText(String scriptText){
		def checksum = calcCheckSum(scriptText)
		loadScript(String.valueOf(checksum), checksum, scriptText)
	}

	/**
	 * Calculates a MD5 Checksum
	 * @param source
	 * @return String
	 */
	private String calcCheckSum(String source){
		DigestUtils.md5Hex source
	}


	/**
	 * 
	 * @param fileName String
	 * @param checksum String
	 * @param scriptSource can be a File or String
	 * @return Script class
	 */
	private Class<Script> loadScript(String fileName, String checksum, scriptSource){

		if(LOG.isDebugEnabled())
			LOG.debug "---------->>> ScriptClassCache loading for $checksum"

		ScriptCacheItem item = checkSumMap[fileName]
		def loadScript = false
		GroovyClassLoader loader

		if(!item){
			item = new ScriptCacheItem()
			loader = new GroovyClassLoader()
			loadScript = true
		}else{
			loader = item.loader
			loadScript = item.checksum != checksum
		}

		if(loadScript){
			if(LOG.isDebugEnabled()){
				Throwable t = new Throwable()
				def traces = Arrays.toString(t.getStackTrace())
				if(traces.size() > 50){
					traces = traces[0..50]
				}
				
				
				LOG.debug "---------->>> ScriptClassCache loading new script $fileName   $traces"
				LOG.debug "---------->>> $scriptSource"
			}
			
			def scriptClass = loader.parseClass(scriptSource)
		    println "ScriptClass: ${scriptClass}  ${scriptClass.class}"
			item.loader = loader
			item.checksum = checksum
			item.script = scriptClass
			item.fileName = fileName
			checkSumMap[fileName] = item
			
		}

		return item.script
	}

	/**
	 * Gets the default static instance of this cache
	 * @return
	 */
	public static final ScriptClassCache getDefaultInstance(){
		return INSTANCE
	}
}
