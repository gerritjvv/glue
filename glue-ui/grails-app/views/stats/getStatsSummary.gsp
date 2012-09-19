<%--
  Created by IntelliJ IDEA.
  User: don
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
   <title>
     ${params.name}: runs per hour
   </title>
        <meta name="layout" content="main" />
  <script type="text/javascript" src="${resource(dir:'js',file:'jquery.colorformatter.js')}"></script>
  <script  type="text/javascript" >


$(document).ready(function(){
<% data.each{ r ->
        %>

    colorformat("#${r.name} .thisday td.units",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .firstXday td.units",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .oldday td.units",'#FFFF00','#00FF00',true);

    colorformat("#${r.name} .thisday td.triggers",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .firstXday td.triggers",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .oldday td.triggers",'#FFFF00','#00FF00',true);

    colorformat("#${r.name} .firstXday .utotal",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .firstXday .ttotal",'#FFFF00','#00FF00',true);

    colorformat("#${r.name} .oldday .utotal",'#FFFF00','#00FF00',true);
    colorformat("#${r.name} .oldday .ttotal",'#FFFF00','#00FF00',true);

     <% } %>
  })
        </script>




</head>
<body>

    <% data.each{ r -> %>

        <div id="${r.name}" class="sidecolumn">
        <g:link action="statsForUnit" params="[name:r.name,days:90]"> <h2>${r.name}</h2></g:link>
        <g:include action="statsByNameForNDays" params="[name:r.name,days:params.days]"/>
        </div>

     <% } %>

 </body>
</html>
