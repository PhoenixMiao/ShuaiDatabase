import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ShuaiObject implements Serializable {

    protected ShuaiObjectType objectType;

    public ShuaiObjectType getObjectType() {
        return objectType;
    }

    public ShuaiReply get(String[] argv, ConcurrentHashMap<ShuaiString, ShuaiObject> dict) {
        return new ShuaiReply(ShuaiReplyStatus.OK, this);
    }
}
