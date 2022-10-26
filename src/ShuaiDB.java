import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShuaiDB implements Serializable {

    static final long serialVersionUID = -8263944406711121676L;

    private final ConcurrentHashMap<ShuaiString,ShuaiObject> dict;

    private transient DelayQueue<ShuaiObject> expires;

    private final ConcurrentHashMap<ShuaiString, Long> lru;

    private final Lock exLock = new ReentrantLock();
    private final Condition condition = exLock.newCondition();

    public ShuaiDB() {
        dict = new ConcurrentHashMap<>();
        expires = new DelayQueue<>();
        lru = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getDict() {
        return dict;
    }

    public DelayQueue<ShuaiObject> getExpires() {
        return expires;
    }

    public void initExpires() {
        this.expires = new DelayQueue<>();
    }

    public Lock getExLock() {
        return exLock;
    }

    public Condition getCondition() {
        return condition;
    }

    public ConcurrentHashMap<ShuaiString, Long> getLru() {
        return lru;
    }
}
