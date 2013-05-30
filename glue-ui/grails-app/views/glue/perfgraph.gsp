<!DOCTYPE html>
<html>
<head>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
<g:javascript src="highcharts.js" />
</head>
<body>


<div id="container" style="width:100%; height:400px;"></div>

<script type="text/javascript">
$(document).ready(function(){
  $("#container").highcharts(
	  {
	        chart: {
	            type: 'line',
	            zoomType: 'y'
	        },
	        title: {
	            text: 'Workflow Performance'
	        },
	        xAxis: {
		        title:{ text: "Last 60 days"}
	      
	        },	
	        yAxis: {
		        min:0,
		        max:60,
	            title: {
	                text: 'Daily AVG Minutes'
	            }
	        },
	        series: 

              <%= (graph as String).replaceAll('=', ':') %>
	     	  
	    }
  );
});
</script>
</body>
</html>
