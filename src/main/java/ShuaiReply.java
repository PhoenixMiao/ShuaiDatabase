import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class ShuaiReply extends ShuaiTalk implements Serializable{

    private ShuaiRequest responsiveRequest;

    private final ShuaiReplyStatus replyStatus;

    private ShuaiErrorCode errorCode;

    private ShuaiObject shuaiObject;

    public ShuaiReply(ShuaiReplyStatus replyStatus,ShuaiErrorCode errorCode) {
        this.replyStatus = replyStatus;
        this.errorCode = errorCode;
    }

    public ShuaiReply(ShuaiReplyStatus replyStatus,ShuaiObject shuaiObject) {
        this.replyStatus = replyStatus;
        this.shuaiObject = shuaiObject;
    }

    public void setResponsiveRequest(ShuaiRequest responsiveRequest) {
        this.responsiveRequest = responsiveRequest;
    }

    public void speakOut() {
        if(errorCode!=null) System.out.println(errorCode);
        else System.out.println(shuaiObject);
    }

    public ShuaiReplyStatus getReplyStatus() {
        return replyStatus;
    }

    @Override
    public String toString() {
        return "ShuaiReply{" +
                "responsiveRequest" + responsiveRequest +
                ", replyStatus=" + replyStatus +
                ", errorCode=" + errorCode +
                ", shuaiObject=" + shuaiObject +
                '}';
    }
}
