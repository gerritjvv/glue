---
layout: doc
title: Hadoop Module Time Series Example
permalink: hadoopTimeSeriesExample.html
category: tutorial
---


{% include nav.markdown %}

#Check date partitioned files are filled for the last 3 hours
    //partions are /mytable/year=2012/month=10/day=01/hour=00 etc.
    if ( ctx.hdfs.timeSeries("3.hours","/mytable", new Date(), 
           "30.minutes", { "year=${it.format('yyyy')}/month=${it.format('MM')}/day=${it.format('dd')}/hour=${it.format('HH')}" }, 
           { it + 1.hours }) ){
           
           println "Partitions for the last 3 hours all have files and all files have been edited more than 30 minutes ago"
    }
           
        	

#Check date partitioned files are filled for the last day and collect the files
	
	def files = []
	if ( ctx.hdfs.timeSeries("1.day","/mytable", new Date(), 
           "30.minutes", { "year=${it.format('yyyy')}/month=${it.format('MM')}/day=${it.format('dd')}/hour=${it.format('HH')}" }, 
           { it + 1.hours }), { date, file ->  files << file ){
           
           println "Partitions for the last 3 hours all have files and all files have been edited more than 30 minutes ago"
    }
    
        	

#Check date partitioned files are filled for the last day and collect the files using UTC time zone
	
	def year = new SimpleDateFormat('yyyy'), month = new SimpleDateFormat('MM'), day = new SimpleDateFormat('dd'), hour = new SimpleDateFormat('HH')
	
	[year, month, day, hour]*.timeZone = TimeZone.getTimeZone("UTC") 
	
	def files = []
	if ( ctx.hdfs.timeSeries("1.day","/mytable", new Date() , 
           "30.minutes", { "year=${year.format(it)}/month=${month.format(it)}/day=${day.format(it)}/hour=${hour.format(it)}" }, 
           { it + 1.hours }), { date, file ->  files << file ){
           
           println "Partitions for the last 3 hours all have files and all files have been edited more than 30 minutes ago"
    }
    
