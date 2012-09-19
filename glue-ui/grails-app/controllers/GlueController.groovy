
import grails.converters.*;

//import com.specificmedia.hadoop.cubes.error.ServerError;

class GlueController {
	
	
    def glueService;
	
	
    def index = { 
        ['url':glueService.getHost(), 'modules':glueService.getModules()]
    }
	
    def list ={
        Map l=glueService.getList();
        ['list':l, 'firstDay':l.keySet().max()];
    }

    def jobInfo={
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
        ['info':glueService.getJobInfo(params.uid),'task':glueService.getTaskInfo(params.uid,params.jobId)];

    }

    def graph={
        ['graph':glueService.getGraph(params.uid)];

    }


}	


