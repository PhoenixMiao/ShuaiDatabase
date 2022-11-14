import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShuaiRequest extends ShuaiTalk implements Serializable {

    static final long serialVersionUID = -5024744294721121676L;

    private final int argc;

    private final String[] argv;

    //todo think of concurrent
    private int db;

    private boolean fake = false;

    public ShuaiRequest(String input) {
        String[] tmpArgv = input.split(" ",2);
        if(tmpArgv.length==0 || !COMMAND_PREFIXES.containsKey(tmpArgv[0])) {
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND).speakOut();
            throw new RuntimeException();
        }
        this.argc = COMMAND_PREFIXES.get(tmpArgv[0]);
        this.argv = new String[this.argc];
        this.argv[0] = tmpArgv[0];
        tmpArgv[1] = tmpArgv[1].trim();
        if(this.argc>1) {
            StringBuilder builder = new StringBuilder(tmpArgv[1]);
            boolean flag = false;
            if(builder.toString().contains("\"")) {
                Deque<Integer> cnt = new ArrayDeque<>();
                for(int i = 0;i<builder.toString().length();i++) {
                    if(builder.toString().charAt(i) == '"' && ( i==0 || builder.toString().charAt(i-1) != '\\')) {
                        if(!cnt.isEmpty()) {
                            int top = cnt.pollLast();
                            for(int j = top + 1;j<i;j++) {
                                if(builder.toString().charAt(j)==' ') {
                                    builder.replace(j,j+1,"\n");
                                    flag = true;
                                }
                            }
                        }else cnt.push(i);
                    }
                }
            }
            String[] bridge = builder.toString().split(" ",this.argc - 1);
            if(bridge.length != this.argc-1) {
                new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.ARGUMENT_WRONG).speakOut();
                throw new RuntimeException();
            }
            if(flag) for(int i = 0;i<bridge.length;i++) {
                bridge[i] = bridge[i].trim().replace('\n',' ');
            }
            System.arraycopy(bridge, 0, this.argv, 1,this.argc - 1);
        }
    }

    public ShuaiRequest(String input,boolean fake){
        this(input);
        this.fake = fake;
    }

    public boolean isFake() {
        return fake;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public int getDb() {
        return db;
    }

    public int getArgc() {
        return argc;
    }

    public String[] getArgv() {
        return argv;
    }

    public static final HashMap<String, Integer> COMMAND_PREFIXES  = new HashMap<String, Integer>(){{
        put("GET", 2);
        put("SET", 3);
        put("GETRANGE", 4);
        put("SETRANGE", 4);
        put("STRLEN", 2);
        put("INCRBY", 3);
        put("INCRBYFLOAT", 3);
        put("DECRBY", 3);
        put("DECRBYFLOAT", 3);
        put("LPUSH", 3);
        put("RPUSH", 3);
        put("LRANGE", 4);
        put("LPOP", 2);
        put("RPOP", 2);
        put("LLEN", 2);
        put("LINDEX", 3);
        put("LINSERT", 5);
        put("LREM", 4);
        put("LTRIM", 4);
        put("LSET", 4);
        put("HGET", 3);
        put("HSET", 4);
        put("HEXIST",3);
        put("HLEN",2);
        put("HGETALL",2);
        put("HDEL",3);
        put("EXPIRE", 3);
        put("PEXPIRE", 3);
        put("DEL",2);
        put("SADD",3);
        put("SCARD",2);
        put("SDIFF",3);
        put("SDIFFSTORE",4);
        put("SINTER",3);
        put("SINTERSTORE",4);
        put("SMEMBERS",2);
        put("SISMEMBER",3);
        put("SMOVE",4);
        put("SREM",3);
        put("SUNION",3);
        put("SUNIONSTORE",4);
        put("SPOP",3);
        put("SRANDMEMBER",3);
        put("ZADD",3);
        put("ZCARD",2);
        put("ZCOUNT",4);
        put("ZINCRBY",3);
        put("ZINTERSTORE",4);
        put("ZRANGEBYSCORE",4);
        put("ZRANGE",4);
        put("ZRANK",3);
        put("ZREM",3);
        put("ZREMRANGEBYRANK",4);
        put("ZREMRANGEBYSCORE",4);

        put("ZREVRANGE",4);
        put("ZREVRANGEBYSCORE",4);
        put("ZREVRANK",3);
        put("ZSCORE",3);
    }};

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for(String argument : argv) res.append(argument).append(" ");
        return res.toString().trim();
    }

    public static void main(String[] args) {
        new ShuaiRequest("GET");
    }
}
