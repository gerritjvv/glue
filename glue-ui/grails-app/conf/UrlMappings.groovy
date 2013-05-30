class UrlMappings {
    static mappings = {


      "/"( controller:"glue", action: "index")
      "/list"( controller:"glue", action: "list")
      "/kill"( controller:"glue", action: "kill")
      "/glue/$uid/?"( controller:"glue", action: "jobInfo")
      "/glue/$uid/$jobId?"( controller:"glue", action: "taskInfo")
      "/graph/$uid/?"( controller:"glue", action: "graph")
      
      "/dsSelector/$action/?"( controller:"dsSelector")
      "/stats/$action/?"( controller:"stats")
	  "/stats/perfgraph"( controller:"glue", action: "perfgraph")
	  
      
      /* 
      "/stats/$action/?"(controller: "stats" )
      
      
       "500"(view:'/error')
       
      "/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
  		*/
  		
    }
}
