import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShuaiDB implements Serializable {

    static final long serialVersionUID = -8263944406711121676L;

    public static final AtomicLong ID = new AtomicLong(0);

    public static final AtomicLong lsmID = new AtomicLong(0);

    private final long id;

    private final ConcurrentHashMap<ShuaiString,ShuaiObject> dict;

    private transient DelayQueue<ShuaiExpireKey> expires;

    private final ConcurrentHashMap<ShuaiString, Long> lru;

    private final ShuaiRedBlackTree lsmTree;

    private final Lock exLock = new ReentrantLock();
    private final Condition condition = exLock.newCondition();

    public ShuaiDB() {
        id = ID.incrementAndGet();
        dict = new ConcurrentHashMap<>();
        expires = new DelayQueue<>();
        lru = new ConcurrentHashMap<>();
        lsmTree = new ShuaiRedBlackTree();
    }

    public ConcurrentHashMap<ShuaiString, ShuaiObject> getDict() {
        return dict;
    }

    public DelayQueue<ShuaiExpireKey> getExpires() {
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

    public ShuaiRedBlackTree getLsmTree() {
        return lsmTree;
    }

    public long getId() {
        return id;
    }

    public ShuaiEntry allKeysLRU() {
        AtomicReference<ShuaiString> min = new AtomicReference<>();
        AtomicLong mint = new AtomicLong(System.currentTimeMillis());
        lru.forEach((k,v) -> {
            if(v < mint.get()) {
                mint.set(v);
                min.set(k);
            }
        });
        if(min.get()==null) return null;
        lru.remove(min.get());
        expires.remove(new ShuaiExpireKey(min.get()));
        ShuaiEntry entry = new ShuaiEntry(min.get(),dict.get(min.get()));
        System.out.println(entry);
        dict.remove(min.get());
        return entry;
//                db.getExpires().remove(min.get());
    }
}
