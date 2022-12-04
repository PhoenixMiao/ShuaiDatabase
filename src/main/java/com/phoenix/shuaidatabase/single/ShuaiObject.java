package com.phoenix.shuaidatabase.single;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ShuaiObject implements Serializable, Delayed {

    protected ShuaiObjectType objectType;

    protected long expireTime;

    public ShuaiObjectType getObjectType() {
        return objectType;
    }

    public ShuaiReply get(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK, this);
    }

    public static ShuaiReply delete(String[] argv, ShuaiDB db) {
        ShuaiString key = new ShuaiString(argv[1]);
        db.getDict().remove(key);
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiObject());
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public ShuaiReply expire(String[] argv, ShuaiDB db) {
        try{
            long time = (long) Integer.parseInt(argv[2]);
            if(time < 0) throw new RuntimeException();
        }catch (Exception e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.EXPIRE_TIME_INPUT_FAULT);
        }
        ShuaiString object = new ShuaiString(argv[1]);
        object.setExpireTime(System.nanoTime() + (long) Integer.parseInt(argv[2]) * ShuaiConstants.ONT_NANO);
        db.getExpires().remove(new ShuaiExpireKey(object));
        db.getExpires().put(new ShuaiExpireKey(object));
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString(argv[2]));
    }

    public ShuaiReply pExpire(String[] argv, ShuaiDB db) {
        try{
            long time = (long) Integer.parseInt(argv[2]);
            if(time < 0) throw new RuntimeException();
        }catch (Exception e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.EXPIRE_TIME_INPUT_FAULT);
        }
        ShuaiString object = new ShuaiString(argv[1]);
        object.setExpireTime(System.nanoTime() + (long) Integer.parseInt(argv[2]) * 1000000);
        db.getExpires().put(new ShuaiExpireKey(object));
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString(argv[2]));
    }

    public ShuaiReply expireAt(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiObject());
    }

    public ShuaiReply pExpireAt(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiObject());
    }

    public static ShuaiReply select(String[] argv, ShuaiDB db) {
        try{
            int dbId = Integer.parseInt(argv[1]);
            Iterator<ShuaiDB> iterator = ShuaiServer.dbs.iterator();
            int cnt = 0;
            while(cnt<dbId && iterator.hasNext()) cnt++;
            ShuaiServer.dbActive = iterator.next();
            return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString("OK"));
        }catch (Exception e){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.NO_SUCH_DATABASE);
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime - System.nanoTime(),TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if(other == this) return 0;
        if(other instanceof ShuaiObject) {
            ShuaiObject x = (ShuaiObject) other;
            long diff = expireTime - x.expireTime;
            if(diff < 0) return -1;
            if(diff > 0) return 1;
            return 1;
        }
        long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
        return (d==0) ? 0:((d<0) ? -1:1);
    }
}
