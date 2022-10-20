import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShuaiCommand {

    public static final ConcurrentHashMap<String,ShuaiCommand> commands = new ConcurrentHashMap<String,ShuaiCommand>(){{
        try {
            put("GET",new ShuaiCommand("GET",ShuaiString.class.getMethod("get"),2,0,0));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }};

    private final String name;

    private final Method proc;

    private final int arity;

    private final AtomicInteger calls;

    private Long milliseconds;

    public ShuaiCommand(String name, Method proc, int arity, int calls, long milliseconds) {
        this.name = name;
        this.proc = proc;
        this.arity = arity;
        this.calls = new AtomicInteger(calls);
        this.milliseconds = milliseconds;
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
}
