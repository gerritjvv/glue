package org.glue.modules.hadoop.pigutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import org.apache.pig.PigServer; 
import org.apache.pig.impl.PigContext; 
import org.apache.pig.impl.util.UDFContext; 
import org.apache.pig.tools.parameters.ParameterSubstitutionPreprocessor; 

/**
 * Utility Class to create and load the Pig Server and its Context Properties
 */
@Typed
public class PigServerUtil {
	
	static public PigServer create(Properties properties) throws IOException{

			String execType = properties.getProperty("exectype", "mapreduce");
			
			//by default warning aggregation is on
			if(properties.getProperty("aggregate.warning")==null)
				properties.setProperty("aggregate.warning", ""+true);

	        //by default multiquery optimization is on
			if(properties.getProperty("opt.multiquery")==null)
	        	properties.setProperty("opt.multiquery", ""+true);

	      //by default we keep going on error on the backend
	        properties.setProperty("stop.on.failure", ""+true);

	        // set up client side system properties in UDF context
	        UDFContext.getUDFContext().setClientSystemProps();
	        
	        if (properties.get("udf.import.list")!=null)
	            PigContext.initializeImportList((String)properties.get("udf.import.list"));

			PigContext pigContext = new PigContext(PigServer.parseExecType(execType), properties);
			return new PigServer(pigContext);
		
	}
	

	
	/**
	 *  returns the stream of final pig script to be passed to Grunt
	 */
	static public BufferedReader runParamPreprocessor(BufferedReader origPigScript, String paramsFile) 
	                                throws org.apache.pig.tools.parameters.ParseException, IOException{
	    ParameterSubstitutionPreprocessor psp = new ParameterSubstitutionPreprocessor(50);

	    
	    StringWriter writer = new StringWriter();
	    String[] paramsFiles=new String[1];
	    paramsFiles[0]=paramsFile;	   
	    psp.genSubstitutedFile (origPigScript, writer, null , paramsFiles);
        return new BufferedReader(new StringReader(writer.toString()));
	}

	static public int getReturnCodeForStats(int[] stats) {
	    if (stats[1] == 0) {
	        // no failed jobs
	        return 0;
	    }
	    else {
	        if (stats[0] == 0) {
	            // no succeeded jobs
	            return 2;
	        }
	        else {
	            // some jobs have failed
	            return 3;
	        }
	    }
	}

}
