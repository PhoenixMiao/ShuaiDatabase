import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShuaiTask implements Callable<String> {

    private SelectionKey key;

    private ShuaiRequest aofRequest;

    public ShuaiTask(SelectionKey key) {
        this.key = key;
    }

    public ShuaiTask(ShuaiRequest aofRequest) {
        this.aofRequest = aofRequest;
    }

    public ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public String call() throws Exception {
        ShuaiRequest request;
        if(aofRequest==null) {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer output = (ByteBuffer) key.attachment();
            output.rewind();
            client.read(output);
            output.rewind();
//        StringBuilder request = new StringBuilder();
//        for(int a = output.get();a!=0;a = output.get()) request.append((char)a);
            request = (ShuaiRequest) ShuaiTalk.backToObject(output.array());
            assert key.isWritable();
            output.rewind();
            ShuaiReply reply = executeMethod(request);
            reply.setResponsiveRequest(request);
            output.put(reply.toBytes());
            System.out.println(request);
            output.rewind();
            client.write(output);
            output.compact();
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }else {
            request = aofRequest;
            ShuaiReply reply = executeMethod(request);
            reply.setResponsiveRequest(request);
            System.out.println(reply);
        }

        return null;
    }

    private ShuaiReply test(ShuaiRequest shuaiRequest) {
        return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString(shuaiRequest.toString()));
    }

    private ShuaiReply executeMethod(ShuaiRequest shuaiRequest) {
        String [] argv = shuaiRequest.getArgv();
        if(!ShuaiCommand.commands.containsKey(argv[0])) return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND);
        ShuaiCommand command = ShuaiCommand.commands.get(argv[0]);
//        if(shuaiRequest.getArgc()!=command.getArity()) return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        long startTime = System.currentTimeMillis();
        try {
            ShuaiReply reply = null;
            Iterator<ShuaiDB> iterator = ShuaiServer.dbs.iterator();
            int cnt = 0;
            ShuaiObject object = null;
            ConcurrentHashMap<ShuaiString,ShuaiObject> dict;
            try{
                while(cnt<shuaiRequest.getDb() && iterator.hasNext()) cnt++;
                dict = iterator.next().getDict();
                object = dict.get(new ShuaiString(shuaiRequest.getArgv()[1]));
            }catch (Exception e){
                return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.FAIL_FAST);
            }
            ShuaiCommand shuaiCommand = ShuaiCommand.commands.get(shuaiRequest.getArgv()[0]);
            if(object==null && !shuaiCommand.isStaticOrNot())
                return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.KEY_NOT_FOUND);
            if(object!=null && shuaiCommand.getType()!=null && object.getObjectType() != shuaiCommand.getType())
                return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.TYPE_FORMAT_FAULT);
            reply = (ShuaiReply) command.getProc().invoke(object, (Object) shuaiRequest.getArgv(),dict);
            if(reply.getReplyStatus()==ShuaiReplyStatus.OK) {
                command.setMilliseconds(System.currentTimeMillis() - startTime);
                command.increaseCalls();
                if(ShuaiCommand.commands.get(shuaiRequest.getArgv()[0]).isWillModify()){
                    if(ShuaiServer.isRdb) executor.submit(new IncreaseDirty());
                    if(ShuaiServer.isAof && !shuaiRequest.isFake()) executor.submit(new AppendOnlyFile(shuaiRequest));
                }
            }
            dict = null;
            return reply;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.REFLECT_INVOKE_METHOD_FAIL);
        }
    }

    static class IncreaseDirty implements Callable<Integer>{

        @Override
        public Integer call() {
            ShuaiServer.lastSave.set(System.currentTimeMillis());
            int result = ShuaiServer.dirty.incrementAndGet();
            if(!ShuaiServer.rdbing.get()) {
                ShuaiServer.r.lock();
                try {
                    for (Integer seconds : ShuaiServer.saveParams.keySet()) {
                        //other threads can reset, so it is roughly checking
                        if((System.currentTimeMillis() - ShuaiServer.lastSave.get()) <= seconds* 1000L &&
                                result >= ShuaiServer.saveParams.get(seconds)) {
                            boolean cas = ShuaiServer.rdbing.compareAndSet(false,true);
                            if(!cas) return result;
                            else break;
                        }
                    }
                    if(!ShuaiServer.rdbing.get()) return result;
                }finally {
                    ShuaiServer.r.unlock();
                }
                File rdbFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.RDB_SUFFIX);
                ShuaiServer.wRdbFile.lock();
                try(
                        FileOutputStream fileStream = new FileOutputStream(rdbFile);
                        ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
                ){
                    if(rdbFile.exists()) rdbFile.delete();
                    rdbFile.createNewFile();
                    objectStream.writeObject(ShuaiServer.dbs);
                } catch (Exception e) {
                    new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.RDB_WRITE_FAIL).speakOut();
                } finally {
                    ShuaiServer.wRdbFile.unlock();
                }
                ShuaiServer.dirty.set(0);
                result = 0;
                if(!ShuaiServer.rdbing.compareAndSet(true,false)) {
                    throw new RuntimeException();
                }
            }
            return result;
        }
    }

    static class AppendOnlyFile implements Runnable {

        private final ShuaiRequest request;

        public AppendOnlyFile(ShuaiRequest request) {
            this.request = request;
        }

        public ShuaiRequest getRequest() {
            return request;
        }

        @Override
        public void run() {
            ShuaiServer.wAofFile.lock();
            File aofFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.AOF_SUFFIX);
            try(
                    FileWriter fileWriter = new FileWriter(aofFile,true);
            ){
                fileWriter.append(request.toString()).append(System.getProperty("line.separator"));
            } catch (Exception e) {
                new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.AOF_WRITE_FAIL).speakOut();
            } finally {
                ShuaiServer.wAofFile.unlock();
            }
        }
    }

}
