<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>

      <title>
        ${info.unitId} - ${info.name} </title>
      <meta name="layout" content="main" />
      
 <script>
$(document).ready(function(){

  $('#listform').submit(function() {
    $.ajax({
   type: "POST",
   url: "../../kill",
   data: $('#listform').serialize(),
   success: function(){$('#listform').html("<p class='warning'>Job killed</p>"); window.location='.'}


       //$('#st').html('Handler for .submit() called. <br />'+  $('#listform').serialize());

 });
    return false;
	})
  })
</script> 

    </head>
    <body>
    <p><a href="../.."> &lt;&lt; Back to main screen</a></p>
    <h1>${info.unitId} - ${info.name}</h1>
    <form id="listform">
    <g:if test="${info.status=='RUNNING'}">
      <input type="hidden" name="joblist" value="${info.unitId}"/>
    <button type="submit" id="killbutton">Kill this job</button>
      </g:if>
      </form>
    <table>
      <tr>
        <td>Id</td>
        <td>${info.unitId}</td>
      </tr>
      <tr>
        <td>Name</td>
        <td>${info.name}</td>
        </tr>
      <tr>
        <td>Started</td>
        <td>${info.startDate}</td>
      </tr>
      <tr>
        <td>Runtime</td>
        <td>${info.endDate}</td>
      </tr>
      <tr>
        <td>Progress</td>
        <td
        >${info.progress}</td>
      </tr>
      <g:if test="${info.context.args!=null}">
      <tr>
        <td>Arguments</td>
        <td><% info.context.args.each{ key, val ->  %> <b>${key}: </b> ${val} <br/> <% } %>
        </td>
      </tr>  
      </g:if>

      <tr>
        <td>Status</td>
        <td class="${info.status}">${info.status}
             <g:if test="${info.status=='FAILED'}">
                              <span class='FAILED'>
                <% info.processes.each{ key, row -> if(row.status=='FAILED') {%>
               ${row.error}
               <%} } %>
          </span>
        </g:if>
        </td>
      </tr>
    </table>

    <table class="list">
      <thead>
      <tr>
        <td>Name</td>
        <td>Progress</td>
        <td>Status</td>
        <td>Description</td>
        <td>Dependencies</td>
        <td>Error</td>
      </tr>
      </thead>
      <tbody>
    <% info.processes.each{ key, row -> %>
             <tr class="${row.status}">
        <td><a href="${key}/" class="loglink">${key}</a></td>
        <td>${row.progress}</td>
        <td>&nbsp;${row.status}</td>
        <td>&nbsp;${row.description}</td>
        <td>&nbsp;${row.dependencies.join(', ')}&nbsp;</td>
        <td>${row.error}</td>
      </tr>

    <%} %>
      </tbody>
           </table>

    <% if( grailsApplication.config.graphViz ) { %>
    <embed src="../../graph/${info.unitId}" type="image/svg+xml" name="graph" class="graph"/>
    <% } else { %>
     <p/>
     <b>"No graphviz library is available please install graphviz via yum or apt-get"</b>
    <% } %>
    </body>
</html>