import groovy.sql.Sql;
import groovyx.gpars.GParsPool

class StatsService {

	StatsService() {
	}

	static transactional = false

	def  dsSelectorService;


	def getSql()  {
		return dsSelectorService.getSqlInstance();
	}

	/**
	 * Returns for all units run during the last 30 days and 60 day daily average execution time history.
	 * The values returned are [{name: workflow name, data: array of data } ]
	 * @return
	 */
	public String getLineGraph(){
		//unique names for units run for the last 30 days
		def names = getStatWorkflowList()
		if(names == null || names.size() < 1){
			def q1 = "select distinct name from units where TIME_TO_SEC(now() - end_date) > 259200 order by start_date desc"
			names = getSql().rows(q1.toString()).collect { it['name'] } as List
			names = names[0..( names.size() > 10 ? 10 : names.size() ) -1]
		}

		def days = 60
		def startDate = (new Date()-days)
		def (year, month, day) = startDate.format("yyyy-MM-dd").split('-')
		
		//		def data = GParsPool.withPool {
		names.collect( { name ->
			name = name.trim()
			[name:"\'$name\'",
				data:selectUnitData(name, days)
			] } )
		//		}

	}

	def getStatWorkflowList(){
		def config = dsSelectorService.getCurrentConfig()
		try{
			return config['stats']['workflows'].split(',') as Set
		}catch(NoSuchMethodError e){
		    return null
		}
	}

	/**
	 * Return the evarage daily execution time for a unit for the last 90 days. 
	 * If no value for a date the value is set to 0.
	 * The values are ordered by date asc.
	 * @param name
	 * @return
	 */
	def selectUnitData(name, days){

		def q1 = "select name, DATE_FORMAT(start_date, '%Y%m%d') as daydate, FLOOR(AVG(TIME_TO_SEC(end_date-start_date)/60)) as avg from units where name = '$name' and start_date > 0 and end_date > 0  group by daydate order by daydate desc limit $days"

		def dateMap = new TreeMap( (new Date()-days .. new Date()).inject([:]) { p,q ->
			def d = q.format('yyyyMMdd') as Integer
			p.put(d, ["'$d'", 0 ]); p} )

		getSql().rows(q1.toString()).each { row ->
			def k = row['daydate'] as Integer
			if(dateMap.containsKey(k) && row['avg'])
				dateMap[k] = ["'$k'", row['avg']]
		}

		dateMap.values()
	}


	def getStatsByUnit(int dayCount) {
		def q="""SELECT name,
            SUM(IF(STATUS = 'WAITING', 1, 0 ) ) AS wcount,
            SUM( IF(STATUS = 'RUNNING', 1, 0 ) ) AS rcount,
            SUM( IF(STATUS = 'FINISHED', 1, 0 ) ) AS fcount,
            SUM( IF(STATUS = 'FAILED', 1, 0 ) ) AS failedcount,
            count( * ) as count
  FROM units
  where now()< start_date + interval $dayCount day
  GROUP BY name
  ORDER BY name""".toString();


		return getSql().rows(q.toString());
	}

	def getTriggerStatsByUnit(int dayCount) {
		def q="""SELECT unit_name AS name, SUM( IF(
STATUS = 'READY', 1, 0 ) ) AS rcount, SUM( IF(
STATUS = 'PROCESSED', 1, 0 ) ) AS pcount, count( * ) AS count
FROM trigger_files
WHERE now( ) < date + INTERVAL $dayCount
DAY
GROUP BY name""".toString();

		return getSql().rows(q.toString());
	}

	def getStatsByHourForUnit(unitName) {
		def q="""SELECT
            date_format(start_date,'%Y-%m-%d') as  day,
            date_format(start_date,'%H') as hour,
            SUM(IF(STATUS = 'WAITING', 1, 0 ) ) AS wcount,
            SUM( IF(STATUS = 'RUNNING', 1, 0 ) ) AS rcount,
            SUM( IF(STATUS = 'FINISHED', 1, 0 ) ) AS fcount,
            SUM( IF(STATUS = 'FAILED', 1, 0 ) ) AS failedcount,
            count( * ) as count
FROM units
where now()< start_date + interval 90 day and name=$unitName
GROUP BY day, hour""";


		return getSql().rows(q);
	}

	def getTriggerStatsByHourForUnit(unitName) {
		def q="""SELECT
            date_format(date,'%Y-%m-%d') as  day,
            date_format(date,'%H') as hour,
            SUM(IF(STATUS = 'READY', 1, 0 ) ) AS rcount,
            SUM( IF(STATUS = 'PROCESSED', 1, 0 ) ) AS pcount,
            count( * ) as count
FROM trigger_files
where now()< date + interval 90 day and unit_name=$unitName
GROUP BY day, hour""";


		return getSql().rows(q);
	}

	def getStatsByHourForUnit(unitName, int days) {
		def q="""SELECT
              date_format(start_date,'%Y-%m-%d') as  day,
              date_format(start_date,'%H') as hour,
              SUM(IF(STATUS = 'WAITING', 1, 0 ) ) AS wcount,
              SUM( IF(STATUS = 'RUNNING', 1, 0 ) ) AS rcount,
              SUM( IF(STATUS = 'FINISHED', 1, 0 ) ) AS fcount,
              SUM( IF(STATUS = 'FAILED', 1, 0 ) ) AS failedcount,
              count( * ) as count
  FROM units
  where now()< start_date + interval $days day and name=$unitName
  GROUP BY day, hour""";


		return getSql().rows(q);
	}

	def getTriggerStatsByHourForUnit(unitName, int days) {
		def q="""SELECT
              date_format(date,'%Y-%m-%d') as  day,
              date_format(date,'%H') as hour,
              SUM(IF(STATUS = 'READY', 1, 0 ) ) AS rcount,
              SUM( IF(STATUS = 'PROCESSED', 1, 0 ) ) AS pcount,
              count( * ) as count
  FROM trigger_files
  where now()< date + interval $days day and unit_name=$unitName
  GROUP BY day, hour""";


		return getSql().rows(q);
	}


	def getUnitList(name, day, hour)
	{
		def q="""
 SELECT
  unit_id,
  date_format(start_date,'%H:%i:%s') as start,
  date_format(end_date,'%H:%i:%s') as end,
  status
  FROM units
  where
  name=$name
  AND date_format(start_date,'%Y-%m-%d')=${day}
  AND date_format(start_date,'%H')=${hour}
  ORDER BY start_date
    """
		return getSql().rows(q);
	}

	def getTriggerFileList(name, day, hour)
	{
		def q="""
 SELECT
  id,
  path,
  date_format(date,'%H:%i:%s') as start,
  status
  FROM trigger_files
  where
  unit_name=$name
  AND date_format(date,'%Y-%m-%d')=${day}
  AND date_format(date,'%H')=${hour}
  ORDER BY date
    """

		return getSql().rows(q);
	}

	def getTriggerCheckpointsByUnit()
	{
		def q="SELECT unit_name, count(*) as num, ROUND(unix_timestamp(now())-unix_timestamp(max(checkpoint))) as minsec,MAX(checkpoint) as checkTime FROM `trigger_checkpoint` group by unit_name order by minsec";
		def out=[];
		def sql=getSql();
		sql.rows(q).each{ row ->
			def r=[:];
			r['unit_name']=row.unit_name;
			r['num']=row.num;
			r['minsec']=row.minsec;
			r['checkTime']=row.checkTime;
			r['minsecStr']=formatSec(row.minsec*1000);
			out << r;
		}
		return out;


	}

	def getTriggerFileDelaysForUnit(def unitName, def numberOfDays){
		def query = """SELECT
        date_format(date,'%Y-%m-%d') as days, 
        MAX(ROUND((UNIX_TIMESTAMP(ts) - UNIX_TIMESTAMP(date))/3600)) as maxdelay, 
        COUNT(*) as overall, 
        SUM(if(ROUND((UNIX_TIMESTAMP(ts) - UNIX_TIMESTAMP(date))/3600)>=3,1,0)) as not_delivered_within_3h,
        100*(SUM(if(ROUND((UNIX_TIMESTAMP(ts) - UNIX_TIMESTAMP(date))/3600)>=3,1,0))/COUNT(*)) as percent 
        FROM trigger_files 
        WHERE date>=now() - interval $numberOfDays day 
        AND unit_name='${unitName}' 
        GROUP BY days 
        ORDER BY days desc;"""

		return getSql().rows(query);
	}

	def getUnitCheckPoints(name)
	{
		def q="SELECT ROUND(unix_timestamp(now())-unix_timestamp(checkpoint)) as minsec, path FROM trigger_checkpoint where unit_name='${name}' order by checkpoint desc"
		def out=[];
		def sql=getSql();
		sql.rows(q).each{ row ->
			def r=[:];
			r['checkpoint']=formatSec(row.minsec);
			r['path']=row.path;
			out << r;
		}
		return out;


	}


	def setTriggerFileStatusToProcessed(def triggerFileIdList){
		changeTriggerFileStatus(triggerFileIdList, "PROCESSED");
	}

	def setTriggerFileStatusToUnprocessed(def triggerFileIdList){
		changeTriggerFileStatus(triggerFileIdList, "UNPROCESSED");
	}

	private def changeTriggerFileStatus(def triggerFileIdList, def newStatus){
		if(triggerFileIdList && triggerFileIdList.size()>0){

			def query = """
            UPDATE trigger_files
            SET status = '${newStatus}'
            WHERE id IN ('${listAsCommaSeparatedString(triggerFileIdList)}')
            """

			getSql().execute(query);
		}
	}

	private def listAsCommaSeparatedString(def input){
		if(input instanceof String) return input
		return input.join("','")
	}


	static public String formatSec(long time)
	{
		def measures=['week': 604800, 'day': 86400, 'hour': 3600,'min': 60, 'second':1]
		long newTime=time/1000 as long;
		if(newTime==0) return "0 sec";
		def out=[]
		// out << time;
		measures.each{k,v ->
			//print "$k, $v  ";
			if(newTime>=v){
				def m = newTime/v as int;
				if(out.size()<3) {
					if(m>1)
					{
						out<<"$m ${k}s"
					}
					else  if(m==1)    {
						out<<"$m ${k}"
					}
				}

			}
			newTime=newTime % v;
		}
		//println out;
		return out.join(', ');

	}
}