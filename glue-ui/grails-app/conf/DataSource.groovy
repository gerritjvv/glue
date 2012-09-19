/*
dataSource {

 dbCreate = "update" // one of 'create', 'create-drop','update'
          driverClassName = "com.mysql.jdbc.Driver"
          username = "glue"
          password = "glue"

          dbCreate = "validate"
          url = "jdbc:mysql://localhost/glue"
          showSql=true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {

            dbCreate = "update" // one of 'create', 'create-drop','update'
          driverClassName = "com.mysql.jdbc.Driver"
          username = "glue"
          password = "glue"

          dbCreate = "validate"
          url = "jdbc:mysql://localhost/glue"
          showSql=true

        }
    }
    test {
        dataSource {
dbCreate = "update" // one of 'create', 'create-drop','update'
          driverClassName = "com.mysql.jdbc.Driver"
          username = "glue"
          password = "glue"

          dbCreate = "validate"
          url = "jdbc:mysql://localhost/glue"
          showSql=true
        }
    }
    production {
        dataSource {
          dbCreate = "update" // one of 'create', 'create-drop','update'
          driverClassName = ""
          username = ""
          password = ""

          dbCreate = "validate"
          url = ""
          showSql=true
        }
    }
}
*/