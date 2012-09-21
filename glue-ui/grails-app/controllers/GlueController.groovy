
import grails.converters.*;

//import com.specificmedia.hadoop.cubes.error.ServerError;

class GlueController {
	
	
    def glueService;
	
	
    def index = { 
        def map = ['url':glueService.getHost(), 'modules':glueService.getModules()]
        return map
    }
	
    def list ={
    	Map l=glueService.getList();
        ['list':l, 'firstDay':l.keySet().max()];
    }

    def jobInfo={
    
        //here we test on each refresh if the graphviz exists, it helps us not having to restart the application
        try{
     		"dot -V".execute()
     		grailsApplication.config.graphViz="yes"
     		
     	}catch(Throwable t){
     		grailsApplication.config.graphViz=null
     	    println "No graphviz library is available please install graphviz via yum or apt-get"
     	}
    
        ['info':glueService.getJobInfo(params.uid)];
    }

    def kill={
        // println params.joblist
        def out=[:]
        if(params.joblist instanceof String)
        {     out[params.joblist]= glueService.kill(params.joblist)
        }
        else
        {
            params.joblist.each{ job ->
                out[job]= glueService.kill(job)
            }
        }
        return ['response':out];
    }

    def taskInfo={
    	println "TaskInfo: "
        ['info':glueService.getJobInfo(params.uid),'task':glueService.getTaskInfo(params.uid,params.jobId)];

    }

    def graph={
        ['graph':glueService.getGraph(params.uid)];

    }


}	


