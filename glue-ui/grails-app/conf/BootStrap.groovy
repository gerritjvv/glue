class BootStrap {

     def grailsApplication

     def init = { servletContext ->
     
     	//check that graphviz is installed
     	try{
     		"dot -V".execute()
     		grailsApplication.config.graphViz="yes"
     		
     	}catch(Throwable t){
     	    println "No graphviz library is available please install graphviz via yum or apt-get"
     	}
     
     }
     
     
     def destroy = {
     }
} 