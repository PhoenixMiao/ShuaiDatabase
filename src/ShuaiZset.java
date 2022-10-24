import java.util.concurrent.ConcurrentHashMap;

public class ShuaiZset extends ShuaiObject{

    private ConcurrentHashMap<ShuaiString,ShuaiString> dict;

    public ShuaiZset() {
        this.dict = new ConcurrentHashMap<>();
        this.ssl = new ShuaiSkipList();
        this.objectType = ShuaiObjectType.SHUAI_ZSET;
    }

    private ShuaiSkipList ssl;

    public int zsetLength() {
        return ssl.getLength();
    }

}
