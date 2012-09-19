package glue.ui
import java.io.*

class DsSelectorController {

    def dsSelectorService

    def setDataSourceNameToSession={

        session["dataSourceName"] = params.dataSourceName
        // Make the singleton dsSelectorService aware of the selected dataSourceName
        dsSelectorService.setCurrentConfigName(params.dataSourceName);
        
        redirect(url:request.getHeader("Referer"))

    }

    //Returns a Map with the keys - list and currentDataSource name
    def loadDsList={
      return([  'list': dsSelectorService.getDataSourceList(),
                'currentDataSourceName':session["dataSourceName"]]);
    }
    
    
    def clearCache = {
        dsSelectorService.databaseCache.clear()
        render "OK"
    }


}