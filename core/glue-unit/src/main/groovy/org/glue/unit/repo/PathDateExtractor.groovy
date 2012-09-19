package org.glue.unit.repo

/**
 * 
 * A default utility class that extracts date values from a repository path.<br/>
 * Dates are embedded inside the path as /mypath/date=2011 or /mypath/year=2011/month=01 etc.<br/>
 *
 */
@Typed(TypePolicy.MIXED)
class PathDateExtractor {

	/**
	 * Extracts the partition values that may or not be a date.<br/>
	 * All fields are treated as case insensitive.
	 * @param path
	 * @return map key=String value=String
	 */
	static Map<String, String> extractFields(String path){
	
		def matcher = path =~ /(([\w\d]+)=+([\w\d-]+))/
		
		def map = [:]
		
		if(matcher.size() > 0){
			//add group 2 and 3 which are [field] and [value]
			matcher.each { map[it[2]] = it[3] }
			
		}
		
		return map
	}
	
	/**
	 * This method support fields year,month,day,hour, date, or daydate.<br/>
	 * Formats supported:<br/>
	 * <ul>
	 *   <li>year=yyyy</li>
	 *   <li>month=MM</li>
	 *   <li>day=dd</li>
	 *   <li>hour=HH</li>
	 *   <li>date=yyyy-MM-dd</li>
	 *   <li>daydate=yyyy-MM-dd</li>
	 * </ul>
	 * 
	 * @param path
	 * @return Date null if no date values were found
	 */
	static Date extractDate(String path){
		
		def map = extractFields(path)
		final Date[] dateBag = [new Date()] as Date[]
		
		if(map){
			
			map.each { key, value ->
				
				Date date = dateBag[0]
				
				def evalKey = key.toString().toLowerCase()
				
				switch(evalKey){
					case 'year':
						date[Calendar.YEAR] = Integer.parseInt(value)
						break
					case 'month':
						//month in calendar starts at 0
					    date[Calendar.MONTH] = Integer.parseInt(value) - 1
						break
					case 'day':
					    date[Calendar.DAY_OF_MONTH] = Integer.parseInt(value)
						break
					case 'hour':
					    date[Calendar.HOUR_OF_DAY] = Integer.parseInt(value)
						break
					case 'date':
					    dateBag[0] = Date.parse('yyyy-MM-dd', value)
						break
					case 'daydate':
					    dateBag[0] = Date.parse('yyyy-MM-dd', value)
						break    
				};
				
			}
			
		}
		
		return dateBag[0]
	}
	
}
