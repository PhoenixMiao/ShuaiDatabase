import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShuaiCommand {

    public static final ConcurrentHashMap<String,ShuaiCommand> commands = new ConcurrentHashMap<String,ShuaiCommand>(){{
        try {
            put("GET",new ShuaiCommand("GET",ShuaiString.class.getMethod("get",String[].class,ConcurrentHashMap.class),2,0,0,false,false));
            put("SET",new ShuaiCommand("SET",ShuaiString.class.getMethod("set",String[].class,ConcurrentHashMap.class),3,0,0,true,true));
            put("GETRANGE", new ShuaiCommand("GETRANGE", ShuaiString.class.getMethod("getRange", String[].class, ConcurrentHashMap.class), 4, 0, 0, false,false));
            put("SETRANGE", new ShuaiCommand("SETRANGE", ShuaiString.class.getMethod("setRange", String[].class, ConcurrentHashMap.class), 4, 0, 0, true,true));
            put("STRLEN", new ShuaiCommand("STRLEN", ShuaiString.class.getMethod("strLen", String[].class, ConcurrentHashMap.class), 2, 0, 0, false,false));
            put("INCRBY", new ShuaiCommand("INCRBY", ShuaiString.class.getMethod("incrBy", String[].class, ConcurrentHashMap.class), 3, 0, 0, false,true));
            put("INCRBYFLOAT", new ShuaiCommand("INCRBYFLOAT", ShuaiString.class.getMethod("incrByFloat", String[].class, ConcurrentHashMap.class), 3, 0, 0, false,true));
            put("DECRBY", new ShuaiCommand("DECRBY", ShuaiString.class.getMethod("decrBy", String[].class, ConcurrentHashMap.class), 3, 0, 0, false,true));
            put("DECRBYFLOAT", new ShuaiCommand("DECRBYFLOAT", ShuaiString.class.getMethod("decrByFloat", String[].class, ConcurrentHashMap.class), 3, 0, 0, false,true));
            put("LPUSH", new ShuaiCommand("LPUSH", ShuaiList.class.getMethod("lPush", String[].class, ConcurrentHashMap.class), 3, 0, 0, true,true));
            put("RPUSH", new ShuaiCommand("RPUSH", ShuaiList.class.getMethod("rPush", String[].class, ConcurrentHashMap.class), 3, 0, 0, true,true));
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

    public ShuaiCommand(String name, Method proc, int arity, int calls, long milliseconds, boolean staticOrNot,boolean willModify) {
        this.name = name;
        this.proc = proc;
        this.arity = arity;
        this.calls = new AtomicInteger(calls);
        this.milliseconds = milliseconds;
        this.staticOrNot = staticOrNot;
        this.willModify = willModify;
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
}
