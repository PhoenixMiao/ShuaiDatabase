import java.util.HashMap;
import java.util.Map;

public enum ShuaiErrorCode {

    COMMAND_NOT_FOUND(1001,"Command not exist."),
    NUMBER_OF_ARGUMENTS_FAULT(1002,"Arguments of command is wrong."),
    REFLECT_INVOKE_METHOD_FAIL(1003,"Fail to invoke method when using java reflection."),
    KEY_NOT_FOUND(1004,"Fail to find the key."),
    FAIL_FAST(1005,"Somebody is creating a new database.Please try later again."),
    ;
    
    private final Integer errorCode;
    
    private final String errorMsg;

    ShuaiErrorCode(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "CommonErrorCode{\n" +
                "errorCode=" + errorCode +
                ",\n errorMsg='" + errorMsg + '\'' +
                '}';
    }

    //use for json serialization
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("errorCode",errorCode);
        map.put("errorMsg",errorMsg);
        return map;
    }


}