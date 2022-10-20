import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public class ShuaiTask implements Callable<String> {

    private SelectionKey key;

    public ShuaiTask(SelectionKey key) {
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer output = (ByteBuffer) key.attachment();
        output.rewind();
        client.read(output);
        output.rewind();
//        StringBuilder request = new StringBuilder();
//        for(int a = output.get();a!=0;a = output.get()) request.append((char)a);
        ShuaiRequest request = (ShuaiRequest) ShuaiRequest.backToObject(output.array());
        assert key.isWritable();
        output.rewind();
        ShuaiReply reply = executeMethod(request);
        output.put(reply.toBytes());
        System.out.println(request);
        output.rewind();
        client.write(output);
        output.compact();
        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        return null;
    }

    private ShuaiReply test(ShuaiRequest shuaiRequest) {
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString(shuaiRequest.toString()));
    }

    private ShuaiReply executeMethod(ShuaiRequest shuaiRequest) {
        String [] argv = shuaiRequest.getArgv();
        if(!ShuaiCommand.commands.containsKey(argv[0])) return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND);
        ShuaiCommand command = ShuaiCommand.commands.get(argv[0]);
        if(shuaiRequest.getArgc()<command.getArity()) return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        long startTime = System.currentTimeMillis();
        try {
            ShuaiReply reply = null;
            ShuaiObject object = ShuaiServer.dbs.get(shuaiRequest.getDb()).getDict().get(new ShuaiString(shuaiRequest.getArgv()[1]));
            if(object==null) return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.KEY_NOT_FOUND);
            if(shuaiRequest.getArgc()==2) {
                reply = (ShuaiReply) command.getProc().invoke(object);
            }else reply = (ShuaiReply) command.getProc().invoke(object, (Object) shuaiRequest.getArgv());
            command.setMilliseconds(System.currentTimeMillis() - startTime);
            command.increaseCalls();
            return reply;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.REFLECT_INVOKE_METHOD_FAIL);
        }
    }

}
