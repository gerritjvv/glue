package org.glue.unit.repo.impl

import org.apache.log4j.Logger
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.repo.GlueUnitRepository

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.MongoClient

/**
 * 
 * 
 *
 */
@Typed(TypePolicy.MIXED)
class MongoDBGlueUnitRepository implements GlueUnitRepository{

	private static final Logger LOG = Logger.getLogger(MongoDBGlueUnitRepository.class)

	final MongoClient client;
	final GlueUnitBuilder unitBuilder;

	final DB db;
	final DBCollection dbCollection;

	final String collection;

	public MongoDBGlueUnitRepository(GlueUnitBuilder glueUnitBuilder, Config config){
		this.unitBuilder = glueUnitBuilder;


		def servers = config.servers.collect { address ->
			final String[] sp = address.split(':')
			new com.mongodb.ServerAddress(
					(sp.length != 2 ) ? new InetSocketAddress(address)
					:new InetSocketAddress(sp[0], Integer.parseInt(sp[1]))
					)
		}

		client = new MongoClient(servers);

		db = client.getDB(config.db);
		collection = config.collection

		if(db == null){
			throw new RuntimeException("Could not find database $db using config: " + config)
		}

		if(config.userName != null){
			if(!db.authenticate(config.userName, config.pwd)){
				throw new RuntimeException("Cannot authenticate to mongo db using config: " + config)
			}
		}

		dbCollection = db.getCollection(collection)
	}

	public void insertOrUpdate(GlueUnit unit){
		dbCollection.findAndModify(new BasicDBObject("name", unit.name),
				new BasicDBObject("name", unit.name).append("data", unitBuilder.mkString(unit)))
	}



	@Override
	public Iterator<GlueUnit> iterator() {
		return new MongoDBIterator(dbCollection.find(), unitBuilder);
	}

	@Override
	public Iterator<GlueUnit> iterator(int from, int max) {
		iterator()
	}

	@Override
	public int size() {
		return (int)dbCollection.count()
	}

	@Override
	public GlueUnit find(String name) {
		def dbObj = dbCollection.findOne(new BasicDBObject("name", name))
		def data = dbObj.get("data")
		if(data != null)
			return unitBuilder.build(data)
		else
			return null
	}

	static class Config{
		final String userName
		final char[] pwd
		final String db;

		final String collection;

		final List<String> servers;

		public Config(String userName, char[] pwd, String db, String collection, List<String> servers) {
			super();
			this.userName = userName;
			this.pwd = pwd;
			this.db = db;
			this.collection = collection;
			this.servers = servers;
		}

		public String toString(){
			"db:$db userName:$userName servers:${servers}"
		}
	}
}
