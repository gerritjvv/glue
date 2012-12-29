package org.glue.unit.om

import org.glue.unit.exceptions.UnitParsingException


/**
 * Builds the GlueUnit implementation
 */
@Typed
interface GlueUnitBuilder {

	GlueUnit build(URL url) throws UnitParsingException;
	GlueUnit build(String config) throws UnitParsingException;
	GlueUnit build(ConfigObject config) throws UnitParsingException;
	String mkString(GlueUnit unit);
	
 }
