package org.glue.gluecron.trigger;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Parses and contains the HDFS History Data Attribute inserted in to mysql Data based on timestampdiff(interval, datetime, CURRENT_TIMESTAMP()) > n.
 * where the expression is "n interval" or "n.interval"
 * 
 */
public class HDFSHistoryDataAttribute {

	final long unitId;
	final String unitName;
	final String path;
	final String dateChooseScript;

	public HDFSHistoryDataAttribute(long unitId, String unitName, String data) {
		this.unitId = unitId;
		this.unitName = unitName;

		int index = data.indexOf(',');
		if (index > 0) {
			path = data.substring(0, index);
			dateChooseScript = parseScript(data.substring(index + 1));
		} else {
			path = data;
			dateChooseScript = null;
		}

	}

	private String parseScript(String substring) {

		String[] split = StringUtils.split(substring, "[. ]");
		final int n = Integer.parseInt(split[0]);
		String interval = null;

		if (split[1] != null) {
			String intStr = split[1].toLowerCase();
			// day, month, year datetime < DATE_SUB(CURDATE(), INTERVAL 1 day)
			// timestampdiff(MONTH, datetime, CURRENT_TIMESTAMP())
			if (intStr.startsWith("second"))
				interval = "SECOND";
			else if (intStr.startsWith("minute"))
				interval = "MINUTE";
			else if (intStr.startsWith("hour"))
				interval = "HOUR";
			else if (intStr.startsWith("day"))
				interval = "DAY";
			else if (intStr.startsWith("week"))
				interval = "WEEK";
			else if (intStr.startsWith("month"))
				interval = "MONTH";
			else if (intStr.startsWith("year"))
				interval = "YEAR";
			else
				throw new RuntimeException("Not expecting " + intStr + " in "
						+ substring);
		} else {
			interval = "DAY";
		}

		return "timestampdiff(" + interval
				+ ", datetime, CURRENT_TIMESTAMP()) >= " + n;
	}

	public long getUnitId() {
		return unitId;
	}

	public String getUnitName() {
		return unitName;
	}

	public String getPath() {
		return path;
	}

	public String getDateChooseScript() {
		return dateChooseScript;
	}

}
