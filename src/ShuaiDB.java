import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ShuaiDB implements Serializable {

    static final long serialVersionUID = -8263944406711121676L;

    //数据库的键值对空间 todo：决定要不要加volatile
    private final ConcurrentHashMap<ShuaiString,ShuaiObject> dict;

    //过期键值对
    private final ConcurrentHashMap<ShuaiString,ShuaiObject> expires;

    public ShuaiDB() {
        dict = new ConcurrentHashMap<>();
        //todo cancel
        dict.put(new ShuaiString("TEST"),new ShuaiString("TTTTTT"));
        expires = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getDict() {
        return dict;
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getExpires() {
        return expires;
    }
}
