<%--
  Created by IntelliJ IDEA.
  User: dimi
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
    colorformat(".thisday td.units",'#FFFF00','#00FF00',true);
    colorformat(".firstXday td.units",'#FFFF00','#00FF00',true);
    colorformat(".oldday td.units",'#FFFF00','#00FF00',true);

    colorformat(".thisday td.triggers",'#FFFF00','#00FF00',true);
    colorformat(".firstXday td.triggers",'#FFFF00','#00FF00',true);
    colorformat(".oldday td.triggers",'#FFFF00','#00FF00',true);

    colorformat(".firstXday .utotal",'#FFFF00','#00FF00',true);
    colorformat(".firstXday .ttotal",'#FFFF00','#00FF00',true);

    colorformat(".oldday .utotal",'#FFFF00','#00FF00',true);
    colorformat(".oldday .ttotal",'#FFFF00','#00FF00',true); 
    
    colorformat(".delay",'#00FF00','#FF0000',true);
    colorformat(".delayPercent",'#00FF00','#FF0000',true);

  })
        </script>
 



</head>  
<body>
  <h1>Hourly runs of ${params.name}</h1>
 <h4>
    <g:link action="getStatsSummary" params="[days:5]">Summary Page</g:link>
</h4>
        <div id="Fivedays" class="sidecolumn">
        <g:include action="statsByNameForNDays" params="[name:params.name,days:numDays]"/>
        </div>

  </body>
</html>