import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShuaiCommand {

    public static final ConcurrentHashMap<String,ShuaiCommand> commands = new ConcurrentHashMap<String,ShuaiCommand>(){{
        try {
            put("GET",new ShuaiCommand("GET",ShuaiString.class.getMethod("get",String[].class,ConcurrentHashMap.class),2,false,false,null));
            put("SET",new ShuaiCommand("SET",ShuaiString.class.getMethod("set",String[].class,ConcurrentHashMap.class),3,true,true,ShuaiObjectType.SHUAI_STRING));
            put("GETRANGE", new ShuaiCommand("GETRANGE", ShuaiString.class.getMethod("getRange", String[].class, ConcurrentHashMap.class), 4, false,false,ShuaiObjectType.SHUAI_STRING));
            put("SETRANGE", new ShuaiCommand("SETRANGE", ShuaiString.class.getMethod("setRange", String[].class, ConcurrentHashMap.class), 4, true,true,ShuaiObjectType.SHUAI_STRING));
            put("STRLEN", new ShuaiCommand("STRLEN", ShuaiString.class.getMethod("strLen", String[].class, ConcurrentHashMap.class), 2, false,false,ShuaiObjectType.SHUAI_STRING));
            put("INCRBY", new ShuaiCommand("INCRBY", ShuaiString.class.getMethod("incrBy", String[].class, ConcurrentHashMap.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("INCRBYFLOAT", new ShuaiCommand("INCRBYFLOAT", ShuaiString.class.getMethod("incrByFloat", String[].class, ConcurrentHashMap.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("DECRBY", new ShuaiCommand("DECRBY", ShuaiString.class.getMethod("decrBy", String[].class, ConcurrentHashMap.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("DECRBYFLOAT", new ShuaiCommand("DECRBYFLOAT", ShuaiString.class.getMethod("decrByFloat", String[].class, ConcurrentHashMap.class), 3, false,true,ShuaiObjectType.SHUAI_STRING));
            put("LPUSH", new ShuaiCommand("LPUSH", ShuaiList.class.getMethod("lPush", String[].class, ConcurrentHashMap.class), 3, true,true,ShuaiObjectType.SHUAI_LIST));
            put("RPUSH", new ShuaiCommand("RPUSH", ShuaiList.class.getMethod("rPush", String[].class, ConcurrentHashMap.class), 3, true,true,ShuaiObjectType.SHUAI_LIST));
            put("LRANGE", new ShuaiCommand("LRANGE", ShuaiList.class.getMethod("lRange", String[].class, ConcurrentHashMap.class), 4, false, false,ShuaiObjectType.SHUAI_LIST));
            put("LPOP", new ShuaiCommand("LPOP", ShuaiList.class.getMethod("lPop", String[].class, ConcurrentHashMap.class), 2, false, true,ShuaiObjectType.SHUAI_LIST));
            put("RPOP", new ShuaiCommand("RPOP", ShuaiList.class.getMethod("rPop", String[].class, ConcurrentHashMap.class), 2,  false, true,ShuaiObjectType.SHUAI_LIST));
            put("LLEN", new ShuaiCommand("LLEN", ShuaiList.class.getMethod("lLen", String[].class, ConcurrentHashMap.class), 2,  false, false,ShuaiObjectType.SHUAI_LIST));
            put("LINDEX", new ShuaiCommand("LINDEX", ShuaiList.class.getMethod("lIndex", String[].class, ConcurrentHashMap.class), 3,  false, false, ShuaiObjectType.SHUAI_LIST));
            put("LINSERT", new ShuaiCommand("LINSERT", ShuaiList.class.getMethod("lInsert", String[].class, ConcurrentHashMap.class), 5,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LREM", new ShuaiCommand("LREM", ShuaiList.class.getMethod("lRem", String[].class, ConcurrentHashMap.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LTRIM", new ShuaiCommand("LTRIM", ShuaiList.class.getMethod("lTrim", String[].class, ConcurrentHashMap.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
            put("LSET", new ShuaiCommand("LSET", ShuaiList.class.getMethod("lSet", String[].class, ConcurrentHashMap.class), 4,  false, true, ShuaiObjectType.SHUAI_LIST));
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
}
