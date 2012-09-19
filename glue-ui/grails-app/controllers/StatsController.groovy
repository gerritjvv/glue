import java.text.DateFormat
import java.text.SimpleDateFormat
class StatsController {
  def statsService;
  def logger;

    def index = {
    //System.out.println("session is"+session["dataSourceName"]);
    }

  def statsByName = {
    def days = params.days ?:3;
    def statsData=statsService.getStatsByUnit(days)
    def triggerData=statsService.getTriggerStatsByUnit(days)
    def triggerMap=[:]
    triggerData.each{ r ->
      triggerMap[r.name]=r;
    }
    def data=[];
    statsData.each{ r->
      def row=r;
      def tEntry=triggerMap[r['name']]?:[:];
      row['trigger_r']=tEntry['rcount']?:0;
      row['trigger_p']=tEntry['pcount']?:0;
      row['trigger_c']=tEntry['count']?:0;
      data << row;
    }
    ['data': data];
  }


  def lastCheckpointByName = {
    ['data':statsService.getTriggerCheckpointsByUnit()]
  }

  def statsForUnit = {
    def c= [ compare:
      {a,b-> b.compareTo(a) }
    ] as Comparator

    def out=[:];
    def out2=[:];
    def tout=[:];
    def tout2=[:];
    def days= new java.util.TreeMap(c);
    def minDay="2999-01-01";
    
    def hours= new java.util.TreeMap();
    def day_param=params.days?:90;
    def data=statsService.getStatsByHourForUnit(params.name,day_param as int);
    def triggerData=statsService.getTriggerStatsByHourForUnit(params.name,day_param as int);
    def triggerFileDelaysData = statsService.getTriggerFileDelaysForUnit(params.name, day_param as int);

    data.each{ r ->
      out2[r.day+" "+r.hour]=r;
      if(r.day<minDay) minDay=r.day;
    }
    triggerData.each{ r ->
      tout2[r.day+" "+r.hour]=r;

      if(r.day<minDay) minDay=r.day;
    }
    (0..23).each{ hourI ->
      hours[String.format("%02d",hourI)]=String.format("%02d",hourI);
    }
    def minTs=Date.parse("yyyy-MM-dd",minDay).getTime()
    def maxTs=new Date().getTime()
    DateFormat f=new SimpleDateFormat("yyyy-MM-dd");
    def ts=minTs;
    while(ts<maxTs)
    {
      def d=f.format(new Date(ts));
      days[d]=d;
      ts+=1000*(24*3600);
    }

    days.keySet().each{ day ->
         hours.keySet().each{ hour ->
           def item=[:];
           if(out2[day+" "+hour])
           {
             item=out2[day+" "+hour];
           }
           else
           {
             item.wcount=0;
             item.rcount=0;
             item.fcount=0;
             item.count=0;
             item.failedcount=0;
           }
           out[day+" "+hour]=item;

          def titem=[:];
           
           if(tout2[day+" "+hour])
           {
             titem=tout2[day+" "+hour];
           }
           else
           {
             titem.rcount=0;
             titem.pcount=0;
             titem.count=0;
           }
           tout[day+" "+hour]=titem;
           //if(titem.rcount>0) System.out.println(titem.rcount);
         }
    }

    ['data' : out,'tdata' : tout, 'hours': hours.keySet(),'days':days.keySet(), 'numDays':day_param, 
    'triggerFileDelaysData':triggerFileDelaysData];
  }

  def statsByNameForNDays = {
    def c= [ compare:
      {a,b-> b.compareTo(a) }
    ] as Comparator

    def out=[:];
    def out2=[:];
    def tout=[:];
    def tout2=[:];
    def days= new java.util.TreeMap(c);
    def minDay="2999-01-01";

    def hours= new java.util.TreeMap();
    def data=statsService.getStatsByHourForUnit(params.name, params.days as int);
    def triggerData=statsService.getTriggerStatsByHourForUnit(params.name,params.days as int);

    data.each{ r ->
      out2[r.day+" "+r.hour]=r;
      if(r.day<minDay) minDay=r.day;
    }
    triggerData.each{ r ->
      tout2[r.day+" "+r.hour]=r;

      if(r.day<minDay) minDay=r.day;
    }
    (0..23).each{ hourI ->
      hours[String.format("%02d",hourI)]=String.format("%02d",hourI);
    }
    def minTs=Date.parse("yyyy-MM-dd",minDay).getTime()
    def maxTs=new Date().getTime()
    DateFormat f=new SimpleDateFormat("yyyy-MM-dd");
    def ts=minTs;
    while(ts<maxTs)
    {
      def d=f.format(new Date(ts));
      days[d]=d;
      ts+=1000*(24*3600);
    }

    days.keySet().each{ day ->
         hours.keySet().each{ hour ->
           def item=[:];
           if(out2[day+" "+hour])
           {
             item=out2[day+" "+hour];
           }
           else
           {
             item.wcount=0;
             item.rcount=0;
             item.fcount=0;
             item.count=0;
             item.failedcount=0;
           }
           out[day+" "+hour]=item;

          def titem=[:];

           if(tout2[day+" "+hour])
           {
             titem=tout2[day+" "+hour];
           }
           else
           {
             titem.rcount=0;
             titem.pcount=0;
             titem.count=0;
           }
           tout[day+" "+hour]=titem;
           //if(titem.rcount>0) System.out.println(titem.rcount);
         }
    }

    ['data' : out,'tdata' : tout, 'hours': hours.keySet(),'days':days.keySet()];
  }

  def getUnitList = {
    ['data': statsService.getUnitList(params.name, params.day,params.hour)];
  }

  def getUnitCheckPoints = {
    ['data': statsService.getUnitCheckPoints(params.name)];
  }

  def getStatsSummary = {

    ['data': statsService.getStatsByUnit(params.days as int)];
  }

  def getTriggerFileList = {
    ['data': statsService.getTriggerFileList(params.name, params.day,params.hour)];
  }
  
  def setTriggerFileStatusToProcessed = {
      statsService.setTriggerFileStatusToProcessed(params.triggerFileIds)
      render(template:"triggerFileList", model:['data': statsService.getTriggerFileList(params.name, params.day,params.hour)])
  }
  
  def setTriggerFileStatusToUnprocessed = {
   statsService.setTriggerFileStatusToUnprocessed(params.triggerFileIds)
   render(template:"triggerFileList", model:['data': statsService.getTriggerFileList(params.name, params.day,params.hour)])   
  }
}
