import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShuaiList extends ShuaiObject{

    private CopyOnWriteArrayList<ShuaiObject> list;

    public ShuaiList() {
        this.list = new CopyOnWriteArrayList<>();
        this.objectType = ShuaiObjectType.SHUAI_LIST;
    }

    public CopyOnWriteArrayList<ShuaiObject> getList() {
        return list;
    }

    public static ShuaiReply lPush(String[] argv, ConcurrentHashMap<ShuaiString, ShuaiObject> dict) {
        ShuaiList shuaiList;
        try{
            if (dict.containsKey(new ShuaiString(argv[1]))) shuaiList = (ShuaiList) dict.get(new ShuaiString(argv[1]));
            else shuaiList = new ShuaiList();
            List<ShuaiObject> newValueList = new ArrayList<>();
            String[] newValue = argv[2].split(" ");
            for (String value : newValue) {
                newValueList.add(new ShuaiString(value));
            }
            for (int i = 2; i < argv.length; i++) {
                shuaiList.list.addAll(newValueList);
            }
            dict.put(new ShuaiString(argv[1]), shuaiList);
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(shuaiList.list.size() + ""));
        } catch(ClassCastException e){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
    }

    public static ShuaiReply rPush(String[] argv, ConcurrentHashMap<ShuaiString, ShuaiObject> dict) {
        ShuaiList shuaiList;  
        if (dict.containsKey(new ShuaiString(argv[1]))) shuaiList = (ShuaiList) dict.get(new ShuaiString(argv[1]));
        else shuaiList = new ShuaiList();
        List<ShuaiObject> newValueList = new ArrayList<>();
        String[] newValue = argv[2].split(" ");
        for (String value : newValue) {
            newValueList.add(0, new ShuaiString(value));
        }
        for (int i = 2; i < argv.length; i++) {
            shuaiList.list.addAll(newValueList);
        }
        dict.put(new ShuaiString(argv[1]), shuaiList);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(shuaiList.list.size() + ""));
    }
}
