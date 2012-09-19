<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
      <title>${info.unitId}/{$task.processName} - ${info.name}</title>
      <meta name="layout" content="main" />
    </head>
    <body>
    <h1>${info.unitId}/${task.processName} - ${info.name}</h1>
    <table>
      <tr>
        <td>Id</td>
        <td><a href="..">${task.unitId}</a></td>
      </tr>
      <tr>
        <td>Name</td>
        <td>${task.processName}</td>
        </tr>
      <tr>
        <td>Started</td>
        <td>${task.startDate}</td>
      </tr>
      <tr>
        <td>Runtime</td>
        <td>${task.endDate}</td>
      </tr>
      <tr>
        <td>Progress</td>
        <td
        >${info.progress}</td>
      </tr>
      <tr>
        <td>Status</td>
        <td class="${info.status}">${info.status}
        </td>
      </tr>
    </table>
    <g:if test="${task.status=='FAILED'}">
      <div class="error">
      <h2>Error:</h2>
       <ol class="output">
      <%

        task.error?.split('\n').eachWithIndex{ line, index ->
        %><li>${line}</li>
      <%
      }
        %>
      </ol>
      </div>
    </g:if>
    <div class="output">
      <h2>Output:</h2>
      <ol class="output">
      <% task.output?.split('\n').eachWithIndex{ line, index ->
        %><li>${line.encodeAsHTML()}</li>
      <%
      }
        %>
      </ol>
      </div>


    </body>
</html>