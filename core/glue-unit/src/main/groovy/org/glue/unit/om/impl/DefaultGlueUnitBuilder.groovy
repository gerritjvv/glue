package org.glue.unit.om.impl

import groovy.util.ConfigObject

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

	/**
	 * Reads the URL source and builds a GlueUnit instance
	 */
	GlueUnit build(URL url) throws UnitParsingException{
		build(ScriptClassCache.getDefaultInstance().parse(new File(url.toURI())))
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
	 * Builds a GlueUnit from a ConfigSluper ConfigObject.
	 * @param config ConfigObject a config object created by ConfigSlurper
	 */
	GlueUnit build(ConfigObject config) throws UnitParsingException{
		return new GlueUnitImpl(config)
	}
}
