import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
public class ShuaiSet extends ShuaiObject{

    private CopyOnWriteArraySet<ShuaiObject> set;

    public ShuaiSet() {
        set = new CopyOnWriteArraySet<ShuaiObject>();
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
            CopyOnWriteArraySet<ShuaiObject> newValueSet = new CopyOnWriteArraySet<ShuaiObject>();
            for (String value : newValue) {
                if(newValueSet.add(new ShuaiString(value))){
                    result++;
                };
            }
            shuaiSet.set.addAll(newValueSet);
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(result+ ""));
        }catch (ClassCastException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
    }

    public ShuaiReply scard(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(set.size()+ ""));
    }


}
