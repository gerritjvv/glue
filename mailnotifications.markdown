---
layout: doc
title: Mail Notifications
permalink: mailnotifications.html
category: tutorial
---


{% include nav.markdown %}


Class: [MailModule](https://github.com/gerritjvv/glue/blob/master/core/glue-modules/src/main/groovy/org/glue/modules/MailModule.groovy)

The mail module when configured will send emails when any workflows marked with notifyOnFail="true" fails.


#Configuration:

Property Name | Description | Example 
 ------ | ----------- | ------- 
recipientList | recipients of the email | myemail@host.com
from_email | the email from field | myserver@host.com
smtp_host | smtp host to use
uiUrl | the base uri to use that points to the glue ui | e.g. http://localhost:8280/glue/glue
getUnitUrl | the url that will be included in the email that points to the glue unit failed | { uiUrl, unitId -> "$uiUrl/$unitId"}
getProcessUrl | the process url included in the email | { uiUrl, unitId, processName -> "$uiUrl/$unitId/$processName"}


#Workflows

add the notifyOnFail="true" property to a workflow 

e.g.
	notifyOnFail="true"
	tasks{
	  myprocess { ctx ->
	  
	  }
	  
	}
	
	
