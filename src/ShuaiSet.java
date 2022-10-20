import java.util.concurrent.ConcurrentHashMap;

public class ShuaiSet extends ShuaiObject{

    private ConcurrentHashMap<ShuaiString, Integer> hashMap;

    public ShuaiSet() {
        this.hashMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<ShuaiString, Integer> getHashMap() {
        return hashMap;
    }

    public void insert(ShuaiString key) {
        hashMap.put(key,1);
    }
}
