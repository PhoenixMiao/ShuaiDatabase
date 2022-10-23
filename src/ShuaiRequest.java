import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShuaiRequest extends ShuaiTalk implements Serializable {

    static final long serialVersionUID = -5024744294721121676L;

    private final int argc;

    private final String[] argv;

    //todo think of concurrent
    private int db;

    public ShuaiRequest(String input) {
        String[] tmpArgv = input.split(" ",2);
        if(tmpArgv.length==0 || !COMMAND_PREFIXES.containsKey(tmpArgv[0])) {
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND).speakOut();
            throw new RuntimeException();
        }
        this.argc = COMMAND_PREFIXES.get(tmpArgv[0]);
        this.argv = new String[this.argc];
        this.argv[0] = tmpArgv[0];
        if(this.argc>1) {
            String[] bridge = tmpArgv[1].split(" ",this.argc - 1);
            System.arraycopy(bridge, 0, this.argv, 1,this.argc - 1);
        }
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
        put("GET",2);
        put("SET",3);
    }};

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for(String argument : argv) res.append(argument);
        return res.toString();
    }
}
