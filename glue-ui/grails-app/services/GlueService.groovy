import java.text.SimpleDateFormat;
import groovy.sql.Sql;
import java.util.Map;
import static groovyx.net.http.Method.GET;
import static groovyx.net.http.ContentType.JSON;
import groovyx.net.http.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * 
 */
class GlueService {

    GlueService(){

    }
    boolean transactional = false
    def config
    String currentConfigName; //test1 or test2
    def c
    def dataSource;
    def logger;

    def http;
    def dsSelectorService;

  
    public String getHost(){
		def config = dsSelectorService.getCurrentConfig()
		
		if(config){ 
        	c= config["glueUrl"];
        	return c.url
        }else{
        	return null;
        }
    }

    public  HTTPBuilder getHttp()
    {
        if(getHost()!=null)
        {
            http = new HTTPBuilder(getHost())
        }
        else
        {
            http = new HTTPBuilder("http://localhost:8025")
        }
        return http;
    }

    public Map getList(){

        def url="${getHost()}/status";
        def out=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = '/status'

            response.success = { resp, json ->
                
                // parse the JSON response object:
                json.each { line ->
                    def ar = [:]
                    def day=new SimpleDateFormat("yyyy-MM-dd").format(new Date(line.value.startDate))
                    ar.uid=line.key;
                    ar.startDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(line.value.startDate))
                    if(line.value.endDate-line.value.startDate>0 && line.value.status!="RUNNING"){
                        ar.endDate=GlueService.formatSec(line.value.endDate-line.value.startDate)
                    }
                    else {
                        ar.endDate=GlueService.formatSec(System.currentTimeMillis()-line.value.startDate)
                    }

                    ar.f =[line.value.startDate,line.value.endDate]
                    ar.name=line.value.name;
                    ar.status=line.value.status;
                    ar.progress=line.value.progress;
                    if(out[day]==null) out[day]=[:]
                    out[day][ar.startDate]=ar;
                }
            }
        }
        out.each{k, v ->
            out.put(k, v.sort{a,b -> b.key<=>a.key});
        };
        return out.sort{a,b ->b.key<=>a.key}
        //return [:]
    }

    public Map getModules(){
		def host = getHost()
		if(!host)
			return [:]
			
        def url="${host}/status";
        def out=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = '/modules'

            response.success = { resp, json ->
                //println resp.statusLine

                // parse the JSON response object:
                json.each { key, value ->
                    out[key]=value.toString(3).replace(' ','&nbsp;');//.replace('\n','<br/>\n');
                }
            }
        }
      
        return out.sort{a,b ->b.key<=>a.key}
    }

    public Map getJobInfo(String jobId){

        def url="${getHost()}/status/${jobId}";
        println url;
        def out=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = "/status/$jobId"

            response.success = { resp, json ->
                //println resp.statusLine
                out= json as java.util.HashMap;
                // parse the JSON response object:

              	if(out.endDate-out.startDate>0 && out.status!="RUNNING"){
                    out.endDate=GlueService.formatSec(out.endDate-out.startDate)
                }
                else {
                    out.endDate=GlueService.formatSec(System.currentTimeMillis()-out.startDate)
                }
                out.startDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(out.startDate))
            }
        }

        return out
    }


    public Map getTaskInfo(String jobId, String taskId){
	Map out=getFromUrl("status/$jobId/$taskId")
        //println out.endDate;
        println GlueService.formatSec(1117);
        if(out.endDate-out.startDate>0){
            println "hahaha1"
            out.endDate=GlueService.formatSec(out.endDate-out.startDate)
            println "hahaha2"
        }
        else {
            out.endDate=GlueService.formatSec(System.currentTimeMillis()-out.startDate)
        }
        out.startDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(out.startDate))
        println out.endDate;
        return out

    }

    public String kill(String jobId)
    {
        def url=getHost()+"/kill/"+jobId;
        //println url;
        def out=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = "/kill/$jobId"

            response.success = { resp, json ->
                //println resp.statusLine
                out= json as java.util.HashMap;

            }
        }

        return out
    }
    public String getGraph(String jobId)
    {
        def url=getHost()+"/status/"+jobId;
        def pList=[:];
        //println url;
        def ou=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = "/status/$jobId"

            response.success = { resp, json ->
                //println resp.statusLine
                ou= json as java.util.HashMap;
                // parse the JSON response object:
                pList=ou.processes  ;
            }
        }


        def p = "dot -Tsvg".execute(); //
        //def out=[];
        def finalKeys=new HashSet();
        finalKeys.addAll(pList.keySet());

        p.withWriter{ out ->

            out <<  "digraph gr{"
            out << "start [shape=box];"
            out << "end [shape=box];"
            pList.each{ key , row ->
                row.dependencies.each{ k -> finalKeys.remove(k)}
                def href="href=\"$key\""
                if(row.status=="RUNNING")
                {
                    out  << "$key [color=\"darkolivegreen1\", label=\"$key (${row.progress*100}%)\"];";
                } else
                if(row.status=="FAILED")
                {
                    out  << "$key [color=\"#7D053F\", class=\"${row.status}\", label=\"$key (${row.progress*100}%)\"];";
                }  else {
                    out  << "$key [class=\"${row.state}\"];";
                }
            }
            println finalKeys;
            pList.each{ key, row ->
                row.dependencies.each{ k ->
                    out << "$k -> $key;"
                }
                if(row.dependencies.size()==0)
                {
                    out << "start -> $key;"
                }
                if(finalKeys.contains(key))
                {
                    out << "$key -> end;"
                }
            }
            out << "}";
        }
        p.waitFor();
        return p.getText();
        //return out.join('\n');


    }

    private Map getFromUrl(request)
    {

        def url=getHost()+"/request";
      	def out=[:]
        http =  getHttp()
        http.request(GET,JSON)
        {
            uri.path = "/$request"

            response.success = { resp, json ->
                //println resp.statusLine
                out= json as java.util.HashMap;
            }
        }
        return out
    }

    static public String formatSec(long time)
    {
        def measures=['week': 604800, 'day': 86400, 'hour': 3600,'min': 60, 'sec':1]
        long newTime=time/1000 as long;
        if(newTime==0) return "0 sec";
        def out=[]
        // out << time;
        measures.each{k,v ->
            //print "$k, $v  ";
            if(newTime>=v){
                def m = newTime/v as int;

                if(m>1)
                {
                    out<<"$m ${k}s"
                }
                else  if(m==1)    {
                    out<<"$m ${k}"
                }

            }
            newTime=newTime % v;
        }
        //println out;
        return out.join(', ');

    }
}
