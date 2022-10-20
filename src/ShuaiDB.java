import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ShuaiDB implements Serializable {

    static final long serialVersionUID = -8263944406711121676L;

    //数据库的键值对空间
    private final ConcurrentHashMap<ShuaiString,ShuaiObject> dict;

    //过期键值对
    private final ConcurrentHashMap<ShuaiString,ShuaiObject> expires;

    public ShuaiDB() {
        dict = new ConcurrentHashMap<>();
        expires = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getDict() {
        return dict;
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getExpires() {
        return expires;
    }
}
