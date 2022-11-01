import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

    private ShuaiReply executeMethod(ShuaiRequest shuaiRequest) {
        String [] argv = shuaiRequest.getArgv();
        if(!ShuaiCommand.commands.containsKey(argv[0])) return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.COMMAND_NOT_FOUND);
        ShuaiCommand command = ShuaiCommand.commands.get(argv[0]);
//        if(shuaiRequest.getArgc()!=command.getArity()) return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        long startTime = System.currentTimeMillis();
        ShuaiDB db;
        try {
            ShuaiReply reply = null;
            Iterator<ShuaiDB> iterator = ShuaiServer.dbs.iterator();
            int cnt = 0;
            ShuaiObject object = null;
            try{
                while(cnt<shuaiRequest.getDb() && iterator.hasNext()) cnt++;
                db = iterator.next();
                ShuaiString key = new ShuaiString(shuaiRequest.getArgv()[1]);
                object = db.getDict().get(new ShuaiString(shuaiRequest.getArgv()[1]));
            }catch (Exception e){
                return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.FAIL_FAST);
            }
            db.getExLock().lock();
            try{
                db.getCondition().signalAll();
            }finally {
                db.getExLock().unlock();
            }
            ShuaiCommand shuaiCommand = ShuaiCommand.commands.get(shuaiRequest.getArgv()[0]);
            System.out.println(shuaiRequest.getArgv().length);
            if(object==null && !shuaiCommand.isStaticOrNot())
                return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.KEY_NOT_FOUND);
            if(object!=null && shuaiCommand.getType()!=null && object.getObjectType() != shuaiCommand.getType())
                return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.TYPE_FORMAT_FAULT);
            if(ShuaiCommand.commands.get(shuaiRequest.getArgv()[0]).isWillModify() && ShuaiServer.reachLimitation
                && ShuaiServer.eliminateStrategy == ShuaiEliminateStrategy.NOEVICTION && !shuaiRequest.getArgv()[0].equals("DEL")) {
                return new ShuaiReply(ShuaiReplyStatus.OUT_OF_MEMORY,ShuaiErrorCode.MEMORY_RAN_OUT_AND_NOEVICTION);
            }
            reply = (ShuaiReply) command.getProc().invoke(object, (Object) shuaiRequest.getArgv(),db);
            if(reply.getReplyStatus()==ShuaiReplyStatus.OK) {
                command.setMilliseconds(System.currentTimeMillis() - startTime);
                command.increaseCalls();
                if(ShuaiCommand.commands.get(shuaiRequest.getArgv()[0]).isWillModify()){
                    //todo more precise!
                    if(!shuaiRequest.getArgv()[0].equals("DEL")) db.getLru().put(new ShuaiString(shuaiRequest.getArgv()[1]),System.currentTimeMillis());
                    if(ShuaiServer.isRdb && !shuaiRequest.isFake() && ShuaiServer.eliminateStrategy!=ShuaiEliminateStrategy.LSM_TREE) executor.submit(new RdbProduce(false));
                    if(ShuaiServer.isAof && !shuaiRequest.isFake() && !shuaiRequest.getArgv()[0].endsWith("EXPIRE")) executor.submit(new AppendOnlyFile(shuaiRequest));
                }
            }
            db = null;
            return reply;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.REFLECT_INVOKE_METHOD_FAIL);
        }
    }

    static class RdbProduce implements Callable<Integer> {

        private final boolean serverCron;

        public RdbProduce(boolean serverCron) {
            this.serverCron = serverCron;
        }

        public boolean isServerCron() {
            return serverCron;
        }

        @Override
        public Integer call() {
            long lastSave = ShuaiServer.lastSave.get();
            long curTime = System.currentTimeMillis();
            ShuaiServer.lastSave.set(curTime);
            int result = 0;
            if(!ShuaiServer.rdbing.get()) {
                ShuaiServer.r.lock();
                try {
                    for (List<Integer> param:ShuaiServer.saveParams) {
                        if(curTime - lastSave > param.get(0) * 1000L) {
                            ShuaiServer.w.lock();
                            try{
                                param.set(2,0);
                            }finally {
                                ShuaiServer.w.unlock();
                            }
                        }else {
                            //other threads can reset, so it is roughly checking
                            if(!serverCron) param.set(2,param.get(2) + 1);
                            result = param.get(2);
                            if((curTime - lastSave) <= param.get(0) * 1000L &&
                                    param.get(2) + 1 >= param.get(1)) {
                                boolean cas = ShuaiServer.rdbing.compareAndSet(false,true);
                                if(!cas) return result;
                                else break;
                            }
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
                if(ShuaiServer.isAof) {
                    ShuaiServer.wAofFile.lock();
                    try(
                            FileWriter fileWriter = new FileWriter(new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.AOF_SUFFIX));
                    ){
                        fileWriter.append("");
                    } catch (Exception e) {
                        new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.AOF_WRITE_FAIL).speakOut();
                    }finally {
                        ShuaiServer.wAofFile.unlock();
                    }
                }
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
