import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ShuaiRequest extends ShuaiTalk implements Serializable {

    static final long serialVersionUID = -5024744294721121676L;

    private final int argc;

    private final String[] argv;

    //todo think of concurrent
    private int db;

    public ShuaiRequest(String input) {
        this.argv = input.split(" ");
        this.argc = argv.length;
        if(argc==0 || !COMMAND_PREFIXES.contains(argv[0])) {
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND).speakOut();
            throw new RuntimeException();
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

    public static final List<String> COMMAND_PREFIXES = Arrays.asList(
            "SET",
            "GET"
    );

    @Override
    public String toString() {
        return "ShuaiCommand{" +
                "argc=" + argc +
                ", argv=" + Arrays.toString(argv) +
                '}';
    }
}
