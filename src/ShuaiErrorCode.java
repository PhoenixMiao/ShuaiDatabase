import java.util.HashMap;
import java.util.Map;

public enum ShuaiErrorCode {

    COMMAND_NOT_FOUND(1001, "Command not exist."),
    NUMBER_OF_ARGUMENTS_FAULT(1002, "Arguments of command is wrong."),
    REFLECT_INVOKE_METHOD_FAIL(1003, "Fail to invoke method when using java reflection."),
    KEY_NOT_FOUND(1004, "Fail to find the key."),
    FAIL_FAST(1005, "Somebody is creating a new database.Please try later again."),
    RDB_LOAD_FAIL(1006, "Fail to load rdbFile.Please check whether your rdbFile exists."),
    AOF_LOAD_FAIL(1007, "Fail to load aofFile.Please check whether your aofFile exists."),
    RDB_WRITE_FAIL(1008, "Fail to write rdbFile.Please check whether your rdbFile exists."),
    AOF_WRITE_FAIL(1009, "Fail to write aofFile.Please check whether your aofFile exists."),
    TYPE_FORMAT_FAULT(1010, "Type of argument is wrong"),
    OUT_OF_RANGE_FAULT(1011, "Index is out of range"),
    ARGUMENT_WRONG(1012,"Input wrong argument"),
    EXPIRE_TIME_INPUT_FAULT(1013,"Expire time has to be a positive integer"),
    EXPIRE_THREAD_INTERRUPTED(1014,"Thread responsible for expire delete is interrupted."),
    MEMORY_RAN_OUT_AND_NOEVICTION(1015,"There is no memory for new entry."),
    LSM_WRITE_FAIL(1016, "Fail to write lsmFile."),
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
