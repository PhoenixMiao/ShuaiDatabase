import java.io.Serializable;
import java.util.Objects;

public class ShuaiString extends ShuaiObject implements Serializable {

    static final long serialVersionUID = -5024744406711121676L;

    private StringBuffer value;

    public StringBuffer getValue() {
        return value;
    }

    public ShuaiString(String value) {
        this.value = new StringBuffer(value);
    }

    public int compareTo(ShuaiString shuaiString) {
        return value.toString().compareTo(shuaiString.getValue().toString());
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public ShuaiReply get() {
        return new ShuaiReply(ShuaiReplyStatus.OK,this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShuaiString that = (ShuaiString) o;
        return Objects.equals(value.toString(), that.value.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toString());
    }
}
