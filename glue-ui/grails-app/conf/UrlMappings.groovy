class UrlMappings {
    static mappings = {


      "/"( controller:"glue", action: "index")
      "/list"( controller:"glue", action: "list")
      "/kill"( controller:"glue", action: "kill")
      "/glue/$uid/?"( controller:"glue", action: "jobInfo")
      "/glue/$uid/$jobId?"( controller:"glue", action: "taskInfo")
      "/graph/$uid/?"( controller:"glue", action: "graph")
      /*
      "/stats/?"(controller: "stats", action: "index") 
      "/stats/$action/?"(controller: "stats" )
      "/dsSelector/$action/?"(controller: "dsSelector")
       "500"(view:'/error')
       
      "/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
  		*/
  		
    }
}
