<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
   <title>Glue units statistics</title>

<meta name="layout" content="main" />
  <body>
<script type="text/javascript" src="${resource(dir:'js',file:'jquery.colorformatter.js')}"></script>
  <script  type="text/javascript" >$(document).ready(function(){
    colorformat("#today td.num_finished",'#FFFF00','#00FF00',true);
    colorformat("#today td.num_running",'#FFFF00','#00FF00',true);
    colorformat("#today td.num_failed",'#FFFF00','#FF0000',true);
    colorformat("#today td.num_total",'#FFFF00','#00FF00',true);
    colorformat("#today td.num_waiting",'#FFFF00','#FF0000',true);

    colorformat("#today td.num_t_ready",'#FFFF00','#FF0000',true);
    colorformat("#today td.num_t_processed",'#FFFF00','#00FF00',true);
    colorformat("#today td.num_t_total",'#FFFF00','#00FF00',true);


    colorformat("#threedays td.num_finished",'#FFFF00','#00FF00',true);
    colorformat("#threedays td.num_running",'#FFFF00','#00FF00',true);
    colorformat("#threedays td.num_failed",'#FFFF00','#FF0000',true);
    colorformat("#threedays td.num_total",'#FFFF00','#00FF00',true);
    colorformat("#threedays td.num_waiting",'#FFFF00','#FF0000',true);

    colorformat("#threedays td.num_t_ready",'#FFFF00','#FF0000',true);
    colorformat("#threedays td.num_t_processed",'#FFFF00','#00FF00',true);
    colorformat("#threedays td.num_t_total",'#FFFF00','#00FF00',true);


    
    colorformat("#lastweek td.num_finished",'#FFFF00','#00FF00',true);
    colorformat("#lastweek td.num_running",'#FFFF00','#00FF00',true);
    colorformat("#lastweek td.num_failed",'#FFFF00','#FF0000',true);
    colorformat("#lastweek td.num_total",'#FFFF00','#00FF00',true);
    colorformat("#lastweek td.num_waiting",'#FFFF00','#FF0000',true);

    colorformat("#lastweek td.num_t_ready",'#FFFF00','#FF0000',true);
    colorformat("#lastweek td.num_t_processed",'#FFFF00','#00FF00',true);
    colorformat("#lastweek td.num_t_total",'#FFFF00','#00FF00',true);

   
    colorformat("#lastmonth td.num_finished",'#FFFF00','#00FF00',true);
    colorformat("#lastmonth td.num_running",'#FFFF00','#00FF00',true);
    colorformat("#lastmonth td.num_failed",'#FFFF00','#FF0000',true);
    colorformat("#lastmonth td.num_total",'#FFFF00','#00FF00',true);
    colorformat("#lastmonth td.num_waiting",'#FFFF00','#FF0000',true);

    colorformat("#lastmonth td.num_t_ready",'#FFFF00','#FF0000',true);
    colorformat("#lastmonth td.num_t_processed",'#FFFF00','#00FF00',true);
    colorformat("#lastmonth td.num_t_total",'#FFFF00','#00FF00',true);

    
    colorformat("#lastq td.num_finished",'#FFFF00','#00FF00',true);
    colorformat("#lastq td.num_running",'#FFFF00','#00FF00',true);
    colorformat("#lastq td.num_failed",'#FFFF00','#FF0000',true);
    colorformat("#lastq td.num_total",'#FFFF00','#00FF00',true);
    colorformat("#lastq td.num_waiting",'#FFFF00','#FF0000',true);

    colorformat("#lastq td.num_t_ready",'#FFFF00','#FF0000',true);
    colorformat("#lastq td.num_t_processed",'#FFFF00','#00FF00',true);
    colorformat("#lastq td.num_t_total",'#FFFF00','#00FF00',true);

    colorformat("td.lastchecksec",'#00FF00','#FF0000',true);
  })
          </script>

  <h1>Glue runs monitoring</h1>
<h4>
    <g:link action="getStatsSummary" params="[days:5]">Summary Page</g:link>
</h4>
  <br>

 <div id="today" class="sidecolumn">
    <h2>Last day:</h2>
    <g:include action="statsByName" params="[days:1]" />
</div>

 <div id="threedays" class="sidecolumn">
   <h2>Last 3 days:</h2>
   <g:include action="statsByName" params="[days:3]" />
 </div>

  <div id="lastweek" class="sidecolumn">
   <h2>Last 7 days:</h2>
   <g:include action="statsByName" params="[days:7]" />
 </div>
  
  <div id="lastmonth" class="sidecolumn">
   <h2>Last 30 days:</h2>
   <g:include action="statsByName" params="[days:30]" />
 </div>

 <!-- <div id="lastq" class="sidecolumn">
  <h2>Last 90 days:</h2>
 <%-- <g:include action="statsByName" params="[days:90]" /> --%>
 </div> -->

  <div id="lastCheck" style="clear:both">
    <h2>Directory last checks:</h2>
    <g:include action="lastCheckpointByName"/>
  </div>

  </body>
</html>