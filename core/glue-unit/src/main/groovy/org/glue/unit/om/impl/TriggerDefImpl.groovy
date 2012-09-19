package org.glue.unit.om.impl

import java.util.Arrays;

import org.glue.unit.om.TriggerDef
import org.glue.unit.exceptions.UnitParsingException

/**
 *
 * Implements the TriggerDef interface<br/>
 * <b>Group Identifier</b><br/>
 * Triggers can refer to e.g. hdfs triggers that can as an extra parameter contain a group identifier that refers it to be from one configured cluster or another.<br/>
 * The TriggerService will have its own configuration containing the activated modules it should use plus any extra information like clusters used etc.
 * e.g.<br/>
 * <pre>
 *  hfdsTrigger{
 *     clusters{
 *       mycluster{
 *          hdfsProperties="myproperties.properties"
 *       }
 *       mycluster1{
 *          hdfsProperties="myproperties1.properties"
 *       }
 *     }
 *  }
 *  
 * </pre>
 * <br/>
 * An hdfs trigger can then refer to the mycluster by using hdfs:mycluster:path
 * <p/>
 * <b>Trigger Format</b><br/>
 * Two formats are supported:<br/.
 * <ul>
 *  <li> type:value type is the trigger module e.g. hdfs, value depends on the trigger module used e.g. mypath/logs/*.bzip2</li>
 *  <li> type:groupIdentifier:value group identifier depends on the trigger module and may be composite or not.</li> 
 * </ul>
 */
@Typed
class TriggerDefImpl implements TriggerDef{
	
	String type
	String value
	
	String groupIdentifier = ""
	
	
	
	/**
	 * Parse the line type:value;type:value<br/>
	 * And returns a list of TriggerDef objects<br/>
	 * @param str
	 * @return TriggerDef[]
	 */
	static TriggerDef[] parse(String triggerStr){
		
		String[] split = triggerStr.split("[;,]");
		
		def triggers = []
		
		split.each { keyValuePair ->
			
			String[] keyValueSplit = keyValuePair.split(":")
			String groupId = ""
			String value
			if(keyValueSplit.size() == 3){
				groupId = keyValueSplit[1]
				value = keyValueSplit[2]
			}else if(keyValueSplit.size() != 2){
				throw new UnitParsingException("Triggers string is not the expected format " + triggerStr + " error found with " + keyValuePair)
			}else{
				value = keyValueSplit[1]
			}
			
			triggers << new TriggerDefImpl(type:keyValueSplit[0], value:value, groupIdentifier:groupId)
			
		}
		
		return triggers
	}
	
}
