package com.phoenix.shuaidatabase.single;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
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
        if(lru.size()==0)return new ShuaiEntry(new ShuaiString(""),new ShuaiObject());
        AtomicReference<ShuaiString> min = new AtomicReference<>();
        AtomicLong mint = new AtomicLong(System.currentTimeMillis());
        ShuaiEntry entry;
        lru.forEach((k,v) -> {
            if(v < mint.get()) {
                mint.set(v);
                min.set(k);
            }
        });
        if(min.get()==null) return null;
        lru.remove(min.get());
        expires.remove(new ShuaiExpireKey(min.get()));
        entry = new ShuaiEntry(min.get(),dict.get(min.get()));
        dict.remove(min.get());
        return entry;
    }

    public ShuaiEntry allKeysRandom() {
        if(dict.size()==0)return new ShuaiEntry(new ShuaiString(""),new ShuaiObject());
        ShuaiString randomKey = dictGetRandomKey(dict);
        lru.remove(randomKey);
        expires.remove(randomKey);
        ShuaiEntry entry = new ShuaiEntry(randomKey,dict.get(randomKey));
        dict.remove(randomKey);
        return entry;
    }

    public ShuaiEntry volatileKeysLRU() {
        if(expires.size()==0 || lru.size()==0)return new ShuaiEntry(new ShuaiString(""),new ShuaiObject());
        AtomicReference<ShuaiString> min = new AtomicReference<>();
        AtomicLong mint = new AtomicLong(System.currentTimeMillis());
        lru.forEach((k,v) -> {
            if(v < mint.get()) {
                mint.set(v);
                min.set(k);
            }
        });
        if(min.get()==null) return null;
        for(ShuaiExpireKey expireKey: expires){
            if(expireKey.getKey().equals(min.get())){
                lru.remove(min.get());
                expires.remove(new ShuaiExpireKey(min.get()));
                ShuaiEntry entry = new ShuaiEntry(min.get(),dict.get(min.get()));
                dict.remove(min.get());
                return entry;
            }
        }
        return null;
    }



    public ShuaiEntry volatileKeysRandom() {
        if(expires.size()==0 || dict.size()==0)return new ShuaiEntry(new ShuaiString(""),new ShuaiObject());
        ShuaiString randomKey = expireGetRandomKey(expires);
        lru.remove(randomKey);
        expires.remove(randomKey);
        ShuaiEntry entry = new ShuaiEntry(randomKey,dict.get(randomKey));
        dict.remove(randomKey);
        return entry;
    }

    public ShuaiString dictGetRandomKey(ConcurrentHashMap<ShuaiString,ShuaiObject> dict)
    {
        if (dict.size() == 0) return null;
        int dictEntry = new Random().nextInt(dict.size());
        Map.Entry randomEntry = null;
        for(Map.Entry entry : dict.entrySet()){
            dictEntry--;
            if(dictEntry<0){
                randomEntry = entry;
                break;
            }
        }
        assert randomEntry != null;
        return (ShuaiString) randomEntry.getKey();
    }

    public ShuaiString expireGetRandomKey(DelayQueue<ShuaiExpireKey> expires)
    {
        if (expires.size() == 0) return null;
        int dictEntry = new Random().nextInt(expires.size());
        ShuaiExpireKey randomKey=null;
        for(ShuaiExpireKey expireKey : expires){
            dictEntry--;
            if(dictEntry<0){
                randomKey = expireKey;
                break;
            }
        }
        return randomKey.getKey();
    }
}
