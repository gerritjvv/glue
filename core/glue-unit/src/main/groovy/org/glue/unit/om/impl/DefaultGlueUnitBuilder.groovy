package org.glue.unit.om.impl

import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.glue.unit.exceptions.UnitParsingException
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.script.ScriptClassCache

/**
 *
 * Builder that creates the GlueUnitImpl with all its dependencies.<br/>
 * If any error during the ConfigSlurper a GroovyRuntimeException will be thrown by groovy.<br/>
 * <p/>
 * This class uses the ScriptClassCache to cache Script classes.<br/>
 * This avoids permgen errors.
 */
@Typed
class DefaultGlueUnitBuilder implements GlueUnitBuilder{

	static final Logger log = Logger.getLogger(DefaultGlueUnitBuilder.class)
	
	/**
	 * Reads the URL source and builds a GlueUnit instance
	 */
	GlueUnit build(URL url) throws UnitParsingException{
		
		final File file = new File(url.getFile())
		final String fileName = file.name
		if(!fileName.endsWith("groovy")){
			
			final String lang = FilenameUtils.getExtension(fileName)
			
			log.info("Creating DSL for script $file lang:$lang")
			/**
			 * Normal script file, i.e. not a DSL.s
			 */
			build(ScriptClassCache.getDefaultInstance().parse("""

				tasks{
                    ${FilenameUtils.removeExtension(fileName)}{
					  script_${lang}=\'\'\'
                      ${file.text}
                      \'\'\'
                    }
                }

			"""		
				))
		}else{
			build(ScriptClassCache.getDefaultInstance().parse(file))
		}
	}

	/**
	 * Builds from a string
	 * @param config String contains the whole GlueUnit definition.
	 */
	GlueUnit build(String config) throws UnitParsingException{

		//we use the class loader here and all of the script and remove metaclass
		//to avoid a groovy permgen error with config slurper
		try{
			return build(ScriptClassCache.getDefaultInstance().parse(config))
		}catch(Exception t){
			throw new UnitParsingException(t)
		}

	}

	/**
	 * Returns the string represenation of this GlueUnit that can be used via a GlueUnitBuilder to parse the a GlueUnit.
	 */
	String mkString(GlueUnit unit){
		unit.mkString()
	}

	/**
	 * Builds a GlueUnit from a ConfigSluper ConfigObject.
	 * @param config ConfigObject a config object created by ConfigSlurper
	 */
	GlueUnit build(ConfigObject config) throws UnitParsingException{
		return new GlueUnitImpl(config)
	}
}
