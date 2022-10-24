import java.util.concurrent.ConcurrentHashMap;

public class ShuaiHash extends ShuaiObject{

    private ConcurrentHashMap<ShuaiString,ShuaiString> hashMap;

    public ShuaiHash() {
        this.hashMap = new ConcurrentHashMap<>();
        this.objectType = ShuaiObjectType.SHUAI_HASH;
    }

    public ConcurrentHashMap<ShuaiString, ShuaiString> getHashMap() {
        return hashMap;
    }
}
