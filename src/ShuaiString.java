import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ShuaiString extends ShuaiObject implements Serializable {

    static final long serialVersionUID = -5024744406711121676L;

    private StringBuffer value;

    public ShuaiString(String value) {
        this.value = new StringBuffer(value);
        this.objectType = ShuaiObjectType.SHUAI_STRING;
    }


    public StringBuffer getValue() {
        return value;
    }

    public static ShuaiReply set(String[] argv, ShuaiDB db) {
        db.getDict().put(new ShuaiString(argv[1]), new ShuaiString(argv[2]));
        return new ShuaiReply(ShuaiReplyStatus.OK, (ShuaiObject) null);
    }

    public ShuaiReply getRange(String[] argv, ShuaiDB db) {
        try {
            int begin = Integer.parseInt(argv[2]);
            int end = Integer.parseInt(argv[3]);
            if (begin < 0) begin += value.length();
            if (end < 0) end += value.length();
            String res;
            if (end == value.length() - 1) res = value.substring(begin);
            else res = value.substring(begin, end + 1);
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res));
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        } catch (StringIndexOutOfBoundsException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.OUT_OF_RANGE_FAULT);
        }
    }

    public static ShuaiReply setRange(String[] argv, ShuaiDB db) {
        try {
            String key = argv[1];
            int offset = Integer.parseInt(argv[2]);
            String newValue = argv[3];
            ShuaiString oldValue = new ShuaiString("");
            if (db.getDict().containsKey(new ShuaiString(key))) oldValue = (ShuaiString) db.getDict().get(new ShuaiString(key));
            oldValue.value.setLength(offset);
            oldValue.value.append(newValue);
            ShuaiString res = new ShuaiString(oldValue.value.length() + "");
            return new ShuaiReply(ShuaiReplyStatus.OK, res);
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }

    }


    public ShuaiReply append(String[] argv, ShuaiDB db) {
        String newValue = value.append(argv[2]).toString();
        db.getDict().put(this, new ShuaiString(newValue));
        String res = newValue.length() + "";
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res));
    }


    public ShuaiReply incrByFloat(String[] argv, ShuaiDB db) {
        try {
            double incr = Double.parseDouble(argv[2]);
            double doubleValue = Double.parseDouble(value.toString());
            doubleValue += incr;
            value = new StringBuffer(doubleValue + "");
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(value.toString()));
    }


    public ShuaiReply decrByFloat(String[] argv, ShuaiDB db) {
        try {
            double decr = Double.parseDouble(argv[2]);
            double doubleValue = Double.parseDouble(value.toString());
            doubleValue -= decr;
            value = new StringBuffer(doubleValue + "");
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(value.toString()));
    }


    public ShuaiReply incrBy(String[] argv, ShuaiDB db) {
        try {
            long incr = Long.parseLong(argv[2]);
            long longValue = Long.parseLong(value.toString());
            longValue += incr;
            value = new StringBuffer(longValue + "");
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(value.toString()));
    }


    public ShuaiReply decrBy(String[] argv, ShuaiDB db) {
        try {
            long decr = Long.parseLong(argv[2]);
            long longValue = Long.parseLong(value.toString());
            longValue -= decr;
            value = new StringBuffer(longValue + "");
        } catch (NumberFormatException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(value.toString()));
    }


    public ShuaiReply strLen(String[] argv, ShuaiDB db) {
        String res = value.length() + "";
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res));
    }


    @Override
    public String toString() {
        return value.toString();
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

    public int compareTo(ShuaiString shuaiString) {
        return value.toString().compareTo(shuaiString.getValue().toString());
    }
}
