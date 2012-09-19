import com.specificmedia.glue.dal.DatabaseCache
import groovy.sql.Sql

/**
* Acts as a cache for the database connections.<br/>
* Used by the DsSelectorService.
*/
class DatabaseService {

    static transactional = false

    def databaseCaches = [:]

    def createCache(String cacheName, Map<String,Object> args, def refreshPeriod) {
       def cache = new DatabaseCache(args, refreshPeriod.toLong())
       databaseCaches.put(cacheName,cache)
       return cache
    }

    def getCache(String name){
       return databaseCaches.get(name)
    }


}
