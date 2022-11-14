import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;


public class ShuaiSet extends ShuaiObject{

    private CopyOnWriteArraySet<ShuaiObject> set;

    public ShuaiSet() {
        set = new CopyOnWriteArraySet<>();
        this.objectType = ShuaiObjectType.SHUAI_SET;
    }

    public static ShuaiReply sadd(String[] argv, ShuaiDB db) {
        ShuaiSet shuaiSet;
        try {
            //判断key是否已经存在
            if (db.getDict().containsKey(new ShuaiString(argv[1])))
                shuaiSet = (ShuaiSet) db.getDict().get(new ShuaiString(argv[1]));
            else {
                shuaiSet = new ShuaiSet();
                db.getDict().put(new ShuaiString(argv[1]), shuaiSet);
            }
            //result记录成功添加的个数
            int result=0;
            String[] newValue = argv[2].split(" ");
            for (String value : newValue) {
                if(shuaiSet.set.add(new ShuaiString(value))){
                    result++;
                }
            }
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(result+ ""));
        }catch (ClassCastException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
    }

    public ShuaiReply scard(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(set.size()+ ""));
    }

    private CopyOnWriteArraySet<ShuaiObject> diff(String[] sets,ShuaiDB db){
        CopyOnWriteArraySet<ShuaiObject> diffSet = new CopyOnWriteArraySet<>();
        diffSet.addAll(this.set);
        for(int i=0;i<sets.length;i++){
            ShuaiObject object = db.getDict().get(new ShuaiString(sets[i]));
            if(object.getClass().equals(ShuaiSet.class)) {
                diffSet.removeAll(((ShuaiSet) object).set);
            }
        }
        return diffSet;
    }

    private CopyOnWriteArraySet<ShuaiObject> inter(String[] sets,ShuaiDB db){
        CopyOnWriteArraySet<ShuaiObject> interSet = new CopyOnWriteArraySet<>();
        interSet.addAll(this.set);
        for(int i=0;i<sets.length;i++){
            ShuaiObject object = db.getDict().get(new ShuaiString(sets[i]));
            if(object.getClass().equals(ShuaiSet.class)) {
                interSet.retainAll(((ShuaiSet) object).set);
            }
        }
        return interSet;
    }

    private CopyOnWriteArraySet<ShuaiObject> union(String[] sets,ShuaiDB db){
        CopyOnWriteArraySet<ShuaiObject> unionSet = new CopyOnWriteArraySet<>();
        unionSet.addAll(this.set);
        for(int i=0;i<sets.length;i++){
            ShuaiObject object = db.getDict().get(new ShuaiString(sets[i]));
            if(object.getClass().equals(ShuaiSet.class)) {
                unionSet.addAll(((ShuaiSet) object).set);
            }
        }
        return unionSet;
    }

    public ShuaiReply sdiff(String[] argv,ShuaiDB db) {
        String[] otherSet = argv[2].split(" ");
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(diff(otherSet, db).toString()));
    }

    //sdiff key destination [key1 key2...]
    public ShuaiReply sdiffstore(String[] argv, ShuaiDB db){
        String[] otherSet = argv[3].split(" ");
        CopyOnWriteArraySet<ShuaiObject> res = diff(otherSet, db);
        String[] mock = new String[3];
        mock[0]="SADD";
        mock[1]=argv[2];
        StringBuffer stringBuffer = new StringBuffer();
        for(ShuaiObject shuaiObject : res){
            stringBuffer.append(shuaiObject.toString()+" ");
        }
        mock[2] = stringBuffer.toString();
        return sadd(mock,db);
    }


    public ShuaiReply sinter(String[] argv, ShuaiDB db){
        String[] otherSet = argv[2].split(" ");
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(inter(otherSet, db).toString()));
    }

    //sinterstore key destination [key1 key2...]
    public ShuaiReply sinterstore(String[] argv, ShuaiDB db){
        String[] otherSet = argv[3].split(" ");
        CopyOnWriteArraySet<ShuaiObject> res = inter(otherSet, db);
        String[] mock = new String[res.size()+2];
        mock[0]="SADD";
        mock[1]=argv[2];
        StringBuffer stringBuffer = new StringBuffer();
        for(ShuaiObject shuaiObject : res){
            stringBuffer.append(shuaiObject.toString()+" ");
        }
        mock[2] = stringBuffer.toString();
        return sadd(mock,db);
    }

    public ShuaiReply sunion(String[] argv, ShuaiDB db){
        String[] otherSet = argv[2].split(" ");
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(union(otherSet, db).toString()));
    }

    //sunionstore key destination [key1 key2...]
    public ShuaiReply sunionstore(String[] argv, ShuaiDB db){
        String[] otherSet = argv[3].split(" ");
        CopyOnWriteArraySet<ShuaiObject> res = union(otherSet, db);
        String[] mock = new String[res.size()+2];
        mock[0]="SADD";
        mock[1]=argv[2];
        StringBuffer stringBuffer = new StringBuffer();
        for(ShuaiObject shuaiObject : res){
            stringBuffer.append(shuaiObject.toString()+" ");
        }
        mock[2] = stringBuffer.toString();
        return sadd(mock,db);
    }

    public ShuaiReply sismember(String[] argv, ShuaiDB db){
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString(this.set.contains(new ShuaiString(argv[2]))?"true":"false"));
    }

    public ShuaiReply smembers(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK, this);
    }

    public ShuaiReply smove(String[] argv, ShuaiDB db) {
        int res=0;
        ShuaiString target = new ShuaiString(argv[3]);
        if(this.set.contains(target)){
            ShuaiObject dest = db.getDict().get(new ShuaiString(argv[2]));
            if(dest.getClass().equals(ShuaiSet.class)){
                ((ShuaiSet) dest).set.add(target);
                this.set.remove(target);
                res++;
            }
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res+""));
    }

    public ShuaiReply srem(String[] argv, ShuaiDB db){
        int res=0;
        String[] removeItems = argv[2].split(" ");
        for(int i=0;i<removeItems.length;i++){
            if(this.set.remove(new ShuaiString(removeItems[i])))res++;
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res+""));
    }

    public ShuaiReply spop(String[] argv, ShuaiDB db){
        int count=Integer.valueOf(argv[2]);
        if(count<=0)return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(""));
        int length = this.set.size();
        StringBuffer reply = new StringBuffer();
        if(count>=length){
            reply.append(this.set.toString());
            this.set.clear();
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply.toString()));
        }
        Object[] tmp =this.set.toArray();
        while(count-->0){
            this.set.remove(tmp[count]);
            reply.append(tmp[count].toString());
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply.toString()));
    }

    public ShuaiReply srandmember(String[] argv, ShuaiDB db){
        int count=Integer.valueOf(argv[2]);
        if(count<=0)return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(""));
        int length = this.set.size();
        StringBuffer reply = new StringBuffer();
        if(count>=length){
            reply.append(this.set.toString());
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply.toString()));
        }
        Iterator<ShuaiObject> iterator = this.set.iterator();
        while(count-->0 && iterator.hasNext()){
            reply.append(iterator.next().toString()+" ");
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply.toString()));
    }

}
