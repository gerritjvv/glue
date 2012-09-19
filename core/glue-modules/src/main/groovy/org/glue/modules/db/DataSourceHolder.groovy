package org.glue.modules.db


import java.util.Map;
import groovy.sql.Sql


@Typed
class DataSourceHolder {

	
	private Map<String, ConfigObject> holder=new HashMap<String,ConfigObject>();
	private Map<String, Sql> sqlHolder=new HashMap<String, Sql>();

	public void init(ConfigObject db) {
		db.each { dbName, ConfigObject conf ->
			holder.put dbName.toString(), conf
		}
	}

	public Sql getSql(String ds) {
		println holder.get(ds);
		if(!sqlHolder.containsKey(ds)) {
			Sql sql = Sql.newInstance(holder.get(ds).host.toString(), holder.get(ds).user.toString(),
					holder.get(ds).pass.toString(), holder.get(ds).driver.toString())
			sqlHolder.put ds, sql;
		}

		return sqlHolder.get(ds);
	}

	public void close(){
		sqlHolder.each { String  name, Sql sql ->
			println "closing connection: $name"
			try{
				sql.close()
			}catch(t){
				t.printStackTrace()
			}
		}
		sqlHolder.clear()
	}

	public void close(String ds) {
		sqlHolder.get(ds)?.close();
		sqlHolder.remove ds;
	}
}
