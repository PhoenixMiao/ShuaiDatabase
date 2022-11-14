import java.io.Serializable;
import java.util.Map;

public class ShuaiEntry implements Map.Entry<ShuaiString,ShuaiObject> , Serializable, Comparable<ShuaiEntry> {

    private ShuaiString key;

    private ShuaiObject value;

    public ShuaiEntry(ShuaiString key, ShuaiObject value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public ShuaiString getKey() {
        return key;
    }

    @Override
    public ShuaiObject getValue() {
        return value;
    }

    @Override
    public ShuaiObject setValue(ShuaiObject value) {
        ShuaiObject old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public int compareTo(ShuaiEntry o) {
        return this.key.compareTo(o.key);
    }

    @Override
    public String toString() {
        return "[ " + key + " , " + value + " ]";
    }
}
