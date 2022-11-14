import java.nio.charset.StandardCharsets;

public class ShuaiConstants {

    public static final String LOGO = "/ ___|| | | | | | | / \\  |_ _|\n\\___ \\| |_| | | | |/ _ \\  | |\n ___) |  _  | |_| / ___ \\ | |\n|____/|_| |_|\\___/_/   \\_\\___|\n";
    public static final byte[] WELCOME = ("WELCOME TO SHUAI DATABASE!\n" + LOGO + "ENTER exit TO EXIT\n").getBytes(StandardCharsets.UTF_8);
    public static final String PERSISTENCE_PATH = "src\\";
    public static final String RDB_SUFFIX = "rdb.txt";
    public static final String AOF_SUFFIX = "aof.txt";
    public static final String NEW_AOF_SUFFIX = "aof1.txt";
    public static final String LSM_SUFFIX = "lsm\\";
    public static final long MAX_MEMORY = 10000000000L;
    public static final long ONT_NANO = 1000000000;
    public static final long MAX_AOF_SIZE = 256 * 1024 * 1024;
}
