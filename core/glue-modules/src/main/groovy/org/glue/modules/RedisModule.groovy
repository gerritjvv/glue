package org.glue.modules

import com.github.jedis.lock.JedisLock;
import groovy.lang.TypePolicy;
import groovy.lang.Typed;
import groovy.util.ConfigObject
import org.glue.unit.exceptions.ModuleConfigurationException;

import java.util.Map;

import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueModule;
import org.glue.unit.om.GlueProcess;
import org.glue.unit.om.GlueUnit;

import redis.clients.jedis.Jedis;

/**
 * https://github.com/xetorthio/jedis
 * Locks are done using https://github.com/abelaska/jedis-lock
 */
@Typed(TypePolicy.MIXED)
class RedisModule implements GlueModule{

	Jedis jedis;
	Map<String, JedisLock> locks = new HashMap<>();

	public void init(ConfigObject config){

	   final String host = config?.host
	   final Object port = config?.port

	   if(!(host && port))
           throw new ModuleConfigurationException("No host or port defined")

	   jedis = new Jedis(host, port as int)
	   
	}

    public boolean lock(String key){
        synchronized (locks) {

            JedisLock lock = locks.get(key)
            if(lock)
                return true;
            else{
                lock = new JedisLock(jedis, 10000, 30000);
                if(lock.acquire()){
                    locks.put(key, lock)
                    return true
                }else{
                    return false
                }
            }
        }
    }
    public void release(String key){
        synchronized (locks){
            JedisLock lock = locks.remove(key)
            if(lock){
                lock.release()
            }
        }
    }

    public String set(String key, String value){
        return jedis.set(key, vlaue)
    }
    public String set(final String key, final String value, final String nxxx,
                      final String expx, final long time){
        return jedis.set(key, value, nxxx, expx, time)
    }
    public String get(final String key){
        return jedis.get(key)
    }
    public Boolean exists(final String key){
        return jedis.exists(key)
    }
    public Long del(final String... keys){
        return jedis.del(keys)
    }
    public Long del(String key){
        return jedis.del(key)
    }
    public String type(final String key){
        return jedis.type(key)
    }
    public Set<String> keys(final String pattern){
        return jedis.keys(pattern)
    }
    public String randomKey(){
        return jedis.randomKey()
    }
    public String rename(final String oldkey, final String newkey){
        return jedis.rename(oldkey, newkey)
    }
    public Long renamenx(final String oldkey, final String newkey){
        return jedis.rename(oldkey, newkey)
    }
    public Long expire(final String key, final int seconds){
        return jedis.expire(key, seconds)
    }
    public Long expireAt(final String key, final long unixTime){
        return jedis.expireAt(key, unixTime)
    }
    public Long ttl(final String key) {
        return jedis.ttl(key)
    }
    public Long move(final String key, final int dbIndex){
        return jedis.move(key, dbIndex)
    }
    public String getSet(final String key, final String value){
        return jedis.getSet(key, value)
    }
    public List<String> mget(final String... keys){
        return jedis.mget(keys)
    }
    public Long setnx(final String key, final String value){
        return jedis.setnx(key, value)
    }
    public String setex(final String key, final int seconds, final String value){
        return jedis.setex(key, seconds, value)
    }
    public String mset(final String... keysvalues){
        return jedis.mset(keysvalues)
    }
    public Long msetnx(final String... keysvalues){
        return jedis.msetnx(keysvalues)
    }
    public Long decrBy(final String key, final long integer){
        return jedis.decrBy(key, integer)
    }
    public Long decr(final String key){
        return jedis.decr(key)
    }
    public Long incrBy(final String key, final long integer){
        return jedis.incrBy(key, integer)
    }
    public Long incr(final String key){
        return jedis.incr(key)
    }
    public Long append(final String key, final String value){
        return jedis.append(key, value)
    }
    public String substr(final String key, final int start, final int end){
        return jedis.substr(key, start, end)
    }
    public Long hset(final String key, final String field, final String value) {
        return jedis.hset(key, field, value)
    }
    public String hget(final String key, final String field){
        return jedis.hget(key, field)
    }
    public Long hsetnx(final String key, final String field, final String value){
        return jedis.hsetnx(key, field, value)
    }
    public String hmset(final String key, final Map<String, String> hash){
        return jedis.hmset(key, hash)
    }
    public List<String> hmget(final String key, final String... fields){
        return jedis.hmget(key, fields)
    }
    public Long hincrBy(final String key, final String field, final long value){
        return jedis.hincrBy(key, field, value)
    }
    public Boolean hexists(final String key, final String field){
        return jedis.hexists(key, field)
    }
    public Long hdel(final String key, final String... fields){
        return jedis.hdel(key, fields)
    }
    public Long hlen(final String key){
        return jedis.hlen(key)
    }
    public Set<String> hkeys(final String key){
        return jedis.hkeys(key)
    }
    public List<String> hvals(final String key){
        return jedis.hvals(key)
    }
    public Map<String, String> hgetAll(final String key) {
        return jedis.hgetAll(key)
    }
    public Long rpush(final String key, final String... strings){
        return jedis.rpush(key, strings)
    }
    public Long lpush(final String key, final String... strings){
        return jedis.lpush(key, strings)
    }
    public Long llen(final String key){
        return jedis.llen(key)
    }
    public List<String> lrange(final String key, final long start, final long end){
        return jedis.lrange(key, start, end)
    }
    public String ltrim(final String key, final long start, final long end){
        return jedis.ltrim(key, start, end)
    }
    public String lindex(final String key, final long index){
        return jedis.lindex(key, index)
    }
    public String lset(final String key, final long index, final String value){
        return jedis.lset(key, index, value)
    }
    public Long lrem(final String key, final long count, final String value){
        return jedis.lrem(key, count, value)
    }
    public String lpop(final String key){
        return jedis.lpop(key)
    }
    public String rpop(final String key){
        return jedis.rpop(key)
    }
    public String rpoplpush(final String srckey, final String dstkey){
        return jedis.rpoplpush(srckey, dstkey)
    }
    public Long sadd(final String key, final String... members){
        return jedis.sadd(key, members)
    }
    public Set<String> smembers(final String key){
        return jedis.smembers(key)
    }
    public Long srem(final String key, final String... members){
        return jedis.srem(key, members)
    }
    public String spop(final String key){
        return jedis.spop(key)
    }
    public Long smove(final String srckey, final String dstkey, final String member){
        return jedis.smove(srckey, dstkey, member)
    }
    public Long scard(final String key){
        return jedis.scard(key)
    }
    public Boolean sismember(final String key, final String member) {
        return jedis.sismember(key, member)
    }
    public Set<String> sinter(final String... keys){
        return jedis.sinter(keys)
    }
    public Long sinterstore(final String dstkey, final String... keys){
        return jedis.sinterstore(dstkey, keys)
    }
    public Set<String> sunion(final String... keys){
        return jedis.sunion(keys)
    }
    public Long sunionstore(final String dstkey, final String... keys){
        return jedis.sunionstore(dstkey, keys)
    }
    public Set<String> sdiff(final String... keys){
        return jedis.sdiff(keys)
    }
    public Long sdiffstore(final String dstkey, final String... keys){
        return jedis.sdiffstore(dstkey, keys)
    }
    public String srandmember(final String key){
        return jedis.srandmember(key)
    }
    public List<String> srandmember(final String key, final int count){
        return jedis.srandmember(key, count)
    }
    public Long zadd(final String key, final double score, final String member){
        return jedis.zadd(key, score, member)
    }
    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        return jedis.zadd(key, scoreMembers)
    }
    public Set<String> zrange(final String key, final long start, final long end){
        return jedis.zrange(key, start, end)
    }
    public Long zrem(final String key, final String... members){
        return jedis.zrem(key, members)
    }
    public Double zincrby(final String key, final double score, final String member){
        return jedis.zincrby(key, score, member)
    }
    public Long zrank(final String key, final String member){
        return jedis.zrank(key, member)
    }
    public Long zrevrank(final String key, final String member){
        return jedis.zrevrank(key, member)
    }
    public Set<String> zrevrange(final String key, final long start, final long end){
        return jedis.zrevrange(key, start, end)
    }
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long end){
        return jedis.zrangeWithScores(key, start, end)
    }
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
        return jedis.zrevrangeWithScores(key, start, end)
    }
    public Long zcard(final String key){
        return jedis.zcard(key)
    }
    public Double zscore(final String key, final String member){
        return jedis.zscore(key, member)
    }
    public String watch(final String... keys){
        return jedis.watch(keys)
    }
    public List<String> sort(final String key){
        return jedis.sort(key)
    }
    public List<String> blpop(final int timeout, final String... keys){
        return jedis.blpop(timeout, keys)
    }
    public List<String> blpop(String... args){
        return jedis.blpop(args)
    }
    public List<String> brpop(String... args){
        return jedis.brop(args)
    }
    public List<String> blpop(String arg){
        return jedis.blpop(arg)
    }
    public List<String> brpop(String arg){
        return jedis.brpop(arg)
    }
    public Long sort(final String key, final String dstkey){
        return jedis.sort(key, dstkey)
    }
    public List<String> brpop(final int timeout, final String... keys){
        return jedis.brpop(timeout, keys)
    }
    public Long zcount(final String key, final double min, final double max){
        return jedis.zcount(key, min, max)
    }
    public Long zcount(final String key, final String min, final String max) {
        return jedis.zcount(key, min, max)
    }
    public Set<String> zrangeByScore(final String key, final double min, final double max){
        return jedis.zrangeByScore(key, min, max)
    }
    public Set<String> zrangeByScore(final String key, final String min, final String max){
        return jedis.zrangeByScore(key, min, max)
    }
    public Set<String> zrangeByScore(final String key, final double min,
                                     final double max, final int offset, final int count){
        return jedis.zrangeByScore(key, min, max, offset, count)
    }
    public Set<String> zrangeByScore(final String key, final String min,
                                     final String max, final int offset, final int count){
        return jedis.zrangeByScore(key, min, max, offset, count)
    }
    public Set<Tuple> zrangeByScoreWithScores(final String key,
                                              final double min, final double max){
        return jedis.zrangeByScoreWithScores(key, min, max)
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key,
                                              final String min, final String max){
        return jedis.zrangeByScoreWithScores(key, min, max)
    }
    public Set<Tuple> zrangeByScoreWithScores(final String key,
                                              final double min, final double max, final int offset,
                                              final int count){
        return jedis.zrangeByScoreWithScores(key, min, max, offset, count)
    }
    public Set<Tuple> zrangeByScoreWithScores(final String key,
                                              final String min, final String max, final int offset,
                                              final int count){
        return jedis.zrangeByScoreWithScores(key, min, max, offset, count)
    }
    public Set<String> zrevrangeByScore(final String key, final double max,
                                        final double min){
        return jedis.zrevrangeByScore(key, max, min)
    }
    public Set<String> zrevrangeByScore(final String key, final String max,
                                        final String min){
        return jedis.zrevrangeByScore(key, max, min)
    }
    public Set<String> zrevrangeByScore(final String key, final double max,
                                        final double min, final int offset, final int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count)
    }
    public Set<String> zrevrangeByScore(final String key, final String max,
                                        final String min, final int offset, final int count) {
        return jedis.zrevrangeByScore(key, max, min, offset, count)
    }
    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
                                                 final double max, final double min){
        return jedis.zrevrangeByScoreWithScores(key, max, min)
    }
    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
                                                 final String max, final String min){
        return jedis.zrevrangeByScoreWithScores(key, max, min)
    }
    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
                                                 final double max, final double min, final int offset,
                                                 final int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count)
    }
    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
                                                 final String max, final String min, final int offset,
                                                 final int count) {
        return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count)
    }
    public Long zremrangeByRank(final String key, final long start,
                                final long end){
        return jedis.zrembrangeByRank(key, start, end)
    }
    public Long zremrangeByScore(final String key, final double start,
                                 final double end){
        return jedis.zremrangeByScore(key, start, end)
    }
    public Long zremrangeByScore(final String key, final String start,
                                 final String end){
        return jedis.zremrangeByScore(key, start, end)
    }
    public Long zunionstore(final String dstkey, final String... sets){
        return jedis.zunionstore(dstkey, sets)
    }
    public Long zinterstore(final String dstkey, final String... sets){
        return jedis.zinterstore(dstkey, sets)
    }
    public Long strlen(final String key){
        return jedis.strlen(key)
    }
    public Long lpushx(final String key, final String... string){
        return jedis.lpushx(key, string)
    }
    public Long persist(final String key){
        return jedis.persist(key)
    }
    public Long rpushx(final String key, final String... string){
        return jedis.rpushx(key, string)
    }
    public String brpoplpush(String source, String destination, int timeout){
        return jedis.brpoplpush(source, destination, timeout)
    }
    public Boolean setbit(String key, long offset, boolean value){
        return jedis.setbit(key, offset, value)
    }
    public Boolean setbit(String key, long offset, String value){
        return jedis.setbit(key, offset, value)
    }
    public Boolean getbit(String key, long offset){
        return jedis.getbit(key, offset)
    }
    public Long setrange(String key, long offset, String value){
        return jedis.setrange(key, offset, value)
    }
    public String getrange(String key, long startOffset, long endOffset){
        return jedis.getrange(key, startOffset, endOffset)
    }
    public Object eval(String script, int keyCount, String... params) {
        return jedis.eval(script, keyCount, params)
    }
    public Object eval(String script){
        return jedis.eval(script)
    }
    public Long bitcount(final String key){
        return jedis.bitcount(key)
    }
    public Long bitcount(final String key, long start, long end){
        return jedis.bitcount(key, start, end)
    }
    public String set(final String key, final String value, final String nxxx) {
        return jedis.set(key, value, nxxx)
    }
    public String set(final String key, final String value, final String nxxx,
                      final String expx, final int time){
        return jedis.set(key, value, nxxx, expx, time)
    }
    public Long pexpire(final String key, final int milliseconds){
        return jedis.pexpire(key, milliseconds)
    }
    public Long pexpireAt(final String key, final long millisecondsTimestamp){
        return jedis.pexpireAt(key, millisecondsTimestamp)
    }
    public Long pttl(final String key){
        return jedis.pttl(key)
    }
    public String psetex(final String key, final int milliseconds,
                         final String value){
        return jedis.psetex(key, milliseconds, value)
    }
    public Jedis jedis(){
        return jedis
    }

    public void configure(String unitId, ConfigObject config){
		
	}
	
	public void onUnitStart(GlueUnit unit, GlueContext context){
		
	}
	public void onUnitFinish(GlueUnit unit, GlueContext context){
		
	}
	public void onUnitFail(GlueUnit unit, GlueContext context){
		
	}

	public Boolean canProcessRun(GlueProcess process, GlueContext context){
		
	}

	public void onProcessStart(GlueProcess process,GlueContext context){}
	public void onProcessFinish(GlueProcess process, GlueContext context){}
	public void onProcessFail(GlueProcess process, GlueContext context, Throwable t){}
	
	public void onProcessKill(GlueProcess process, GlueContext context){}
	
	public String getName(){
		return "redis";
	}
	
	public void destroy(){

        try {
            locks.each { k, v -> v.release() }
        }catch(Exception e){
            e.printStackTrace()
        }

        try {
            jedis.close();
        }catch(Exception e){
            e.printStackTrace()
        }

    }
	
	public Map getInfo(){
		
	}
	
	
}
