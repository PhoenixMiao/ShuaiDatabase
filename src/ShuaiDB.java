import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;
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
        if(ShuaiServer.eliminateStrategy == ShuaiEliminateStrategy.LSM_TREE) lsmTree = new ShuaiRedBlackTree();
        else lsmTree = null;
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
}
