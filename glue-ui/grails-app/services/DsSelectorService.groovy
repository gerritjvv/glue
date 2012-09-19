import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovy.sql.Sql;
import com.specificmedia.glue.dal.DatabaseCache;

class DsSelectorService  implements org.springframework.beans.factory.InitializingBean{

    boolean transactional = false
    def config
	
	/**
	* servers { serv1 { glueUrl=... } }
	*/
	def grailsApplication
	    
	
    String currentConfigName; //test1 or test2
    
    def databaseService

    DsSelectorService()    {
                
    }

	def void afterPropertiesSet(){
	    config = grailsApplication.config?.servers
            
        def keySet = config?.keySet() as ArrayList
        if(keySet){
          currentConfigName = keySet[0]
        }else{
        	log.error("No servers configured, please ensure that the GLUE_UI_CONFIG env or -D property variable points to a valid config file")
        	log.error("Config1: " + grailsApplication.config)
        	
        }
	}
	
    public getCurrentConfig()
    {
        return (config) ? config[currentConfigName] : null;
    }

    public setCurrentConfigName(String curConfig)
    {
        this.currentConfigName=curConfig;
    }

    public getDataSourceList()
    {
        return config?.keySet()
    }

    def getSqlInstance()  {
        def currentConfig = getCurrentConfig()
        
        if(currentConfig){
        	def c= currentConfig["glueDb"];
        	def sql = Sql.newInstance(c?.url,c?.username,c?.password,c?.driverClassName);
        	return sql;
        }else{
        	return null
        }
    }

    def getMapOfDatabaseConnectionArgs(){
        def currentConfig = getCurrentConfig()
        
        if(currentConfig){
        def c = currentConfig["glueDb"];
        	return [url:c?.url,user:c?.username,password:c?.password,driverClassName:c?.driverClassName]
        }else{
        	return null;
        }
    }


     def getDatabaseCache(){
     
        def databaseCache = databaseService.getCache(currentConfigName)
        
        if(databaseCache==null){
         def refreshPeriod = getCurrentConfig()["glueDb"].cacheRefreshPeriod
         if(!refreshPeriod.toString().isNumber()){
             refreshPeriod=300
         }
         databaseCache = databaseService.createCache(currentConfigName,getMapOfDatabaseConnectionArgs(),refreshPeriod)
        }
        return databaseCache
        
    }

}
