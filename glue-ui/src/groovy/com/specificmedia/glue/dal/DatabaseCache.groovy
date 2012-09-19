package com.specificmedia.glue.dal

import org.apache.commons.logging.LogFactory
import java.util.concurrent.*
import groovy.sql.Sql

class DatabaseCache {
    
    static final log = LogFactory.getLog(this)
    static final DEFAULT_REFRESH_PERIOD_IN_SECONDS = 300 
    
    final def dataCache = new ConcurrentHashMap()

    def url
    def user
    def password
    def driverClassName
    
    Sql sql

    
    DatabaseCache(Map<String,Object> args, Long refreshPeriod){
        url = args.url
        user = args.user
        password = args.password
        driverClassName = args.driverClassName
        sql = Sql.newInstance(url,user,password,driverClassName)
        initiatePeriodicRefresh(refreshPeriod)
    }
    
    def retrieve(String query) {
        return retrieve(query, null)
    }
    
    def retrieve(String query, List<Object> params) {
        def dataset = dataCache.get([query,params]);
        if (dataset == null) {      
            def dbResult = retrieveFromDatabase(query, params)
            dataset = dataCache.putIfAbsent([query,params], dbResult);
            if(dataset==null){
                dataset = dbResult
            }
        }
        return dataset;
    }
    
    private def retrieveFromDatabase(String query, List<Object> params){
        if(sql==null||sql.connection.isClosed()){
            sql = Sql.newInstance(url,user,password,driverClassName)
        }
        if(query && params){
            return sql.rows(query,params)
        }
        else if(query){
            return sql.rows(query)
        }
    }
    
    
    def clear(){
        dataCache.clear()        
    } 
    
    
    
    def initiatePeriodicRefresh(Long refreshPeriod){
        if(!refreshPeriod) refreshPeriod = DEFAULT_REFRESH_PERIOD_IN_SECONDS
      
        Executors.newScheduledThreadPool(4).scheduleAtFixedRate(
            { 
                try{              
                    log.debug "Cache ${sql.hashCode()}: Start refresh of ${dataCache.size()} items at ${new Date().timeString}" 
             
                    dataCache.keySet().each{ key ->
                        def query = key[0]
                        def params = key[1]
                        dataCache.replace(key,retrieveFromDatabase(key[0],key[1]))
                    }
                  
          
                   log.debug "Cache ${sql.hashCode()}: End refresh at ${new Date().timeString}" 
                }
                catch(e){
                    println e
                }
   
            },
            refreshPeriod,refreshPeriod, TimeUnit.SECONDS
        ) 
    }
    

	
}

