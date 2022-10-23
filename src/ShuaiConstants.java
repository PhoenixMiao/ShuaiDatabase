import java.nio.charset.StandardCharsets;

public class ShuaiConstants {

    public static final String LOGO = "/ ___|| | | | | | | / \\  |_ _|\n\\___ \\| |_| | | | |/ _ \\  | |\n ___) |  _  | |_| / ___ \\ | |\n|____/|_| |_|\\___/_/   \\_\\___|\n";
    public static final byte[] WELCOME = ("WELCOME TO SHUAI DATABASE!\n" + LOGO + "ENTER quit TO QUIT\n").getBytes(StandardCharsets.UTF_8);
    public static final String PERSISTENCE_PATH = "src\\";
    public static final String RDB_SUFFIX = "rdb.txt";
    public static final String AOF_SUFFIX = "aof.txt";
    public static final String NEW_AOF_SUFFIX = "aof1.txt";
}
