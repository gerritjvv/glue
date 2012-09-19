<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<h2 style="display: inline;">List of trigger files on ${params.day}, ${params.hour}h</h2>
<g:javascript library="jquery"/>

<g:form>
    <span class="triggerFileActionButtons">
  Set checked to:
 
  <g:set var="error500Message" value="An error occured while trying to update. Status not changed"/>
  
 <g:submitToRemote url="[action:'setTriggerFileStatusToProcessed']"
                    value="Processed" update="triggerFileList"
                     onFailure="if (XMLHttpRequest.status==500) alert('${error500Message}')"
                     />
 
  <g:submitToRemote url="[action:'setTriggerFileStatusToUnprocessed']"
                    value="Unprocessed" update="triggerFileList"
                    onFailure="if (XMLHttpRequest.status==500) alert('${error500Message}')"
                    />
  
  <g:each in="${params}">
    <g:hiddenField name="${it.key}" value="${it.value}" />
  </g:each>
     
  <a href="#" onclick="$('.triggerFileIds').attr('checked',true); return false;">Check All</a>
  <a href="#" onclick="$('.triggerFileIds').attr('checked',false); return false;">Uncheck All</a>
        
  </span>
<div id="triggerFileList">
<g:render template="triggerFileList" model="['data':data]"/>
</g:form>
  