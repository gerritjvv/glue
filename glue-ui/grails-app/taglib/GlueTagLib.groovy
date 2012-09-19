class GlueTagLib {

    static namespace = "glue"

    def grailsApplication



    def pluginMenuItems = { attr, body ->
          grailsApplication.controllerClasses.each { controllerArtefact ->
            if(controllerArtefact.hasProperty("menuItem")){
               def menuItem = controllerArtefact.getPropertyValue("menuItem")
              out << "<li>"
              out << link([controller: controllerArtefact.getLogicalPropertyName(), action: menuItem.action?:"index"],  { menuItem.name  })
              out << "</li>"
            }
        }
        
   
    }

}
