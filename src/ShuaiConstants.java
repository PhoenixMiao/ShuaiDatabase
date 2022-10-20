import java.nio.charset.StandardCharsets;

public class ShuaiConstants {

    public static final String LOGO = "/ ___|| | | | | | | / \\  |_ _|\n\\___ \\| |_| | | | |/ _ \\  | |\n ___) |  _  | |_| / ___ \\ | |\n|____/|_| |_|\\___/_/   \\_\\___|\n";
    public static final byte[] WELCOME = ("WELCOME TO SHUAI DATABASE!\n" + LOGO + "ENTER quit TO QUIT\n").getBytes(StandardCharsets.UTF_8);
}
