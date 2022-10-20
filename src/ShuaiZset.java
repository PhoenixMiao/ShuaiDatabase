import java.util.concurrent.ConcurrentHashMap;

public class ShuaiZset {

    private ConcurrentHashMap<ShuaiString,ShuaiString> dict;

    private ShuaiSkipList ssl;

    public int zsetLength() {
        return ssl.getLength();
    }

}
