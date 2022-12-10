package com.phoenix.shuaidatabase.single;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShuaiCommand {

    public static final ConcurrentHashMap<String,ShuaiCommand> commands = new ConcurrentHashMap<String,ShuaiCommand>(){{
        try {
            put("GET",new ShuaiCommand("GET",ShuaiObject.class.getMethod("get",String[].class,ShuaiDB.class),2,false,false,null));
            put("SET",new ShuaiCommand("SET",ShuaiString.class.getMethod("set",String[].class,ShuaiDB.class),3,true,true,ShuaiObjectType.SHUAI_STRING));
            put("GETRANGE", new ShuaiCommand("GETRANGE", ShuaiString.class.getMethod("getRange", String[].class, ShuaiDB.class), 4, false,false,ShuaiObjectType.SHUAI_STRING));
            put("SETRANGE", new ShuaiCommand("SETRANGE", ShuaiString.class.getMethod("setRange", String[].class, ShuaiDB.class), 4, true,true,ShuaiObjectType.SHUAI_STRING));
            put("STRLEN", new ShuaiCommand("STRLEN", ShuaiString.class.getMethod("strLen", String[].class, ShuaiDB.class), 2, false,false,ShuaiObjectType.SHUAI_STRING));
            put("INCRBY", new ShuaiCommand("INCRBY", ShuaiString.class.getMethod("incrBy", String[].class, ShuaiDB.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("INCRBYFLOAT", new ShuaiCommand("INCRBYFLOAT", ShuaiString.class.getMethod("incrByFloat", String[].class, ShuaiDB.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("DECRBY", new ShuaiCommand("DECRBY", ShuaiString.class.getMethod("decrBy", String[].class, ShuaiDB.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("DECRBYFLOAT", new ShuaiCommand("DECRBYFLOAT", ShuaiString.class.getMethod("decrByFloat", String[].class, ShuaiDB.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("LPUSH", new ShuaiCommand("LPUSH", ShuaiList.class.getMethod("lPush", String[].class, ShuaiDB.class), 3, true,true,ShuaiObjectType.SHUAI_LIST));
            put("RPUSH", new ShuaiCommand("RPUSH", ShuaiList.class.getMethod("rPush", String[].class, ShuaiDB.class), 3, true,true,ShuaiObjectType.SHUAI_LIST));
            put("LRANGE", new ShuaiCommand("LRANGE", ShuaiList.class.getMethod("lRange", String[].class, ShuaiDB.class), 4, false, false,ShuaiObjectType.SHUAI_LIST));
            put("LPOP", new ShuaiCommand("LPOP", ShuaiList.class.getMethod("lPop", String[].class, ShuaiDB.class), 2, false, true,ShuaiObjectType.SHUAI_LIST));
            put("RPOP", new ShuaiCommand("RPOP", ShuaiList.class.getMethod("rPop", String[].class, ShuaiDB.class), 2,  false, true,ShuaiObjectType.SHUAI_LIST));
            put("LLEN", new ShuaiCommand("LLEN", ShuaiList.class.getMethod("lLen", String[].class, ShuaiDB.class), 2,  false, false,ShuaiObjectType.SHUAI_LIST));
            put("LINDEX", new ShuaiCommand("LINDEX", ShuaiList.class.getMethod("lIndex", String[].class, ShuaiDB.class), 3,  false, false, ShuaiObjectType.SHUAI_LIST));
            put("LINSERT", new ShuaiCommand("LINSERT", ShuaiList.class.getMethod("lInsert", String[].class, ShuaiDB.class), 5,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LREM", new ShuaiCommand("LREM", ShuaiList.class.getMethod("lRem", String[].class, ShuaiDB.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LTRIM", new ShuaiCommand("LTRIM", ShuaiList.class.getMethod("lTrim", String[].class, ShuaiDB.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LSET", new ShuaiCommand("LSET", ShuaiList.class.getMethod("lSet", String[].class, ShuaiDB.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("HGET", new ShuaiCommand("HGET", ShuaiHash.class.getMethod("hGet", String[].class, ShuaiDB.class), 3,  false, false, ShuaiObjectType.SHUAI_HASH));
            put("HSET", new ShuaiCommand("HSET", ShuaiHash.class.getMethod("hSet", String[].class, ShuaiDB.class), 4,  true, true, ShuaiObjectType.SHUAI_HASH));
            put("HEXIST", new ShuaiCommand("HEXIST", ShuaiHash.class.getMethod("hExist", String[].class, ShuaiDB.class), 3,  false, false, ShuaiObjectType.SHUAI_HASH));
            put("HLEN", new ShuaiCommand("HLEN", ShuaiHash.class.getMethod("hLen", String[].class, ShuaiDB.class), 2,  false, false, ShuaiObjectType.SHUAI_HASH));
            put("HGETALL", new ShuaiCommand("HGETALL", ShuaiHash.class.getMethod("hGetAll", String[].class, ShuaiDB.class), 2,  false, false, ShuaiObjectType.SHUAI_HASH));
            put("HDEL", new ShuaiCommand("HDEL", ShuaiHash.class.getMethod("hDel", String[].class, ShuaiDB.class), 3,  false, true, ShuaiObjectType.SHUAI_HASH));
            put("HMSET", new ShuaiCommand("HMSET", ShuaiHash.class.getMethod("hMSet", String[].class, ShuaiDB.class), 4,  true, true, ShuaiObjectType.SHUAI_HASH));
            put("EXPIRE", new ShuaiCommand("EXPIRE", ShuaiObject.class.getMethod("expire", String[].class, ShuaiDB.class), 3,  false, true, null));
            put("PEXPIRE", new ShuaiCommand("PEXPIRE", ShuaiObject.class.getMethod("pExpire", String[].class, ShuaiDB.class), 3,  false, true, null));
            put("DEL",new ShuaiCommand("DEL",ShuaiObject.class.getMethod("delete", String[].class, ShuaiDB.class),2,true,true,null));
            put("SADD",new ShuaiCommand("SADD",ShuaiSet.class.getMethod("sadd", String[].class, ShuaiDB.class),3,true,true,ShuaiObjectType.SHUAI_SET));
            put("SCARD",new ShuaiCommand("SCARD",ShuaiSet.class.getMethod("scard", String[].class, ShuaiDB.class),2,false,false,ShuaiObjectType.SHUAI_SET));
            put("SDIFF",new ShuaiCommand("SDIFF",ShuaiSet.class.getMethod("sdiff", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_SET));
            put("SDIFFSTORE",new ShuaiCommand("SDIFFSTORE",ShuaiSet.class.getMethod("sdiffstore", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_SET));
            put("SUNION",new ShuaiCommand("SUNION",ShuaiSet.class.getMethod("sunion", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_SET));
            put("SUNIONSTORE",new ShuaiCommand("SUNIONSTORE",ShuaiSet.class.getMethod("sunionstore", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_SET));
            put("SINTER",new ShuaiCommand("SINTER",ShuaiSet.class.getMethod("sinter", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_SET));
            put("SINTERSTORE",new ShuaiCommand("SINTERSTORE",ShuaiSet.class.getMethod("sinterstore", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_SET));
            put("SMEMBERS",new ShuaiCommand("SMEMBERS",ShuaiSet.class.getMethod("smembers", String[].class, ShuaiDB.class),2,false,false,ShuaiObjectType.SHUAI_SET));
            put("SMOVE",new ShuaiCommand("SMOVE",ShuaiSet.class.getMethod("smove", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_SET));
            put("SISMEMBER",new ShuaiCommand("SISMEMBER",ShuaiSet.class.getMethod("sismember", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_SET));
            put("SREM",new ShuaiCommand("SREM",ShuaiSet.class.getMethod("srem", String[].class, ShuaiDB.class),3,false,true,ShuaiObjectType.SHUAI_SET));
            put("SPOP",new ShuaiCommand("SPOP",ShuaiSet.class.getMethod("spop", String[].class, ShuaiDB.class),3,false,true,ShuaiObjectType.SHUAI_SET));
            put("SRANDMEMBER",new ShuaiCommand("SRANDMEMBER",ShuaiSet.class.getMethod("srandmember", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_SET));
            put("ZADD",new ShuaiCommand("ZADD",ShuaiZset.class.getMethod("zadd", String[].class, ShuaiDB.class),3,true,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZCARD",new ShuaiCommand("ZCARD",ShuaiZset.class.getMethod("zsetLength",String[].class, ShuaiDB.class),2,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZCOUNT",new ShuaiCommand("ZCOUNT",ShuaiZset.class.getMethod("zcount", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZINCRBY",new ShuaiCommand("ZINCRBY",ShuaiZset.class.getMethod("zincrby", String[].class, ShuaiDB.class),3,false,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZINTERSTORE",new ShuaiCommand("ZINTERSTORE",ShuaiZset.class.getMethod("zinterstore", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZRANGEBYSCORE",new ShuaiCommand("ZRANGEBYSCORE",ShuaiZset.class.getMethod("zrangeByScore", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZRANGE",new ShuaiCommand("ZRANGE",ShuaiZset.class.getMethod("zrange", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZRANK",new ShuaiCommand("ZRANK",ShuaiZset.class.getMethod("zrank", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZREM",new ShuaiCommand("ZREM",ShuaiZset.class.getMethod("zrem", String[].class, ShuaiDB.class),3,false,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZREMRANGEBYRANK",new ShuaiCommand("ZREMRANGEBYRANK",ShuaiZset.class.getMethod("zremRangeByRank", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZREMRANGEBYSCORE",new ShuaiCommand("ZREMRANGEBYSCORE",ShuaiZset.class.getMethod("zremRangeByScore", String[].class, ShuaiDB.class),4,false,true,ShuaiObjectType.SHUAI_ZSET));
            put("ZREVRANGE",new ShuaiCommand("ZREVRANGE",ShuaiZset.class.getMethod("zrevRange", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZREVRANGEBYSCORE",new ShuaiCommand("ZREVRANGEBYSCORE",ShuaiZset.class.getMethod("zrevRangeByScore", String[].class, ShuaiDB.class),4,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZREVRANK",new ShuaiCommand("ZREVRANK",ShuaiZset.class.getMethod("zrevRank", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("ZSCORE",new ShuaiCommand("ZSCORE",ShuaiZset.class.getMethod("zscore", String[].class, ShuaiDB.class),3,false,false,ShuaiObjectType.SHUAI_ZSET));
            put("SELECT",new ShuaiCommand("SELECT",ShuaiObject.class.getMethod("select",String[].class,ShuaiDB.class),2,true,true,null));
            put("TTL",new ShuaiCommand("TTL",ShuaiObject.class.getMethod("ttl",String[].class,ShuaiDB.class),2,false,false,null));
            put("PTTL",new ShuaiCommand("PTTL",ShuaiObject.class.getMethod("pttl",String[].class,ShuaiDB.class),2,false,false,null));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }};

    private final String name;

    private final Method proc;

    private final int arity;

    private final AtomicInteger calls;

    private Long milliseconds;

    private final boolean staticOrNot;

    private final boolean willModify;

    private final ShuaiObjectType type;

    public ShuaiCommand(String name, Method proc, int arity, boolean staticOrNot,boolean willModify,ShuaiObjectType type) {
        this.name = name;
        this.proc = proc;
        this.arity = arity;
        this.calls = new AtomicInteger(0);
        this.staticOrNot = staticOrNot;
        this.willModify = willModify;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Method getProc() {
        return proc;
    }

    public int getArity() {
        return arity;
    }

    public boolean isStaticOrNot() {
        return staticOrNot;
    }

    public int getCalls() {
        return calls.get();
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int increaseCalls() {
        return this.calls.incrementAndGet();
    }

    public boolean isWillModify() {
        return willModify;
    }

    public ShuaiObjectType getType() {
        return type;
    }

    public static void main(String[] args) {
        for(ShuaiCommand command : commands.values()) {
            if(command.isWillModify()) System.out.println(command.name);
        }
    }
}
