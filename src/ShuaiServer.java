import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class ShuaiServer {

    public static int DEFAULT_PORT = 8888;

    public static ExecutorService executor = Executors.newFixedThreadPool(100);

    public static ConcurrentLinkedDeque<ShuaiDB> dbs = new ConcurrentLinkedDeque<ShuaiDB>(){{
        add(new ShuaiDB());
        add(new ShuaiDB());
        add(new ShuaiDB());
    }};

    static Boolean isAof = true;
    static Boolean isRdb = true;

    public static AtomicInteger dirty = new AtomicInteger(0);

    public static AtomicLong lastSave = new AtomicLong(System.currentTimeMillis());

    public static Map<Integer,Integer> saveParams = new HashMap<Integer,Integer>(){{
        put(900,100);
        put(300,1000);
        put(60,10000);
        put(10,5);
    }};

    static final ReentrantReadWriteLock saveParamsLock = new ReentrantReadWriteLock();

    static final Lock r = saveParamsLock.readLock();
    static final Lock w = saveParamsLock.writeLock();

    static AtomicBoolean rdbing = new AtomicBoolean(false);

    static final ReentrantReadWriteLock rdbLock = new ReentrantReadWriteLock();

    static final Lock rRdbFile = rdbLock.readLock();
    static final Lock wRdbFile = rdbLock.writeLock();

    static final ReentrantReadWriteLock aofLock = new ReentrantReadWriteLock();

    static final Lock rAofFile = aofLock.readLock();
    static final Lock wAofFile = aofLock.writeLock();

    public static void main(String[] args) {
        System.out.println("Listening for connections on port 8888");
        //for initialize
        ShuaiCommand shuaiCommand;
        ShuaiServer shuaiServer;

        ServerSocketChannel serverSocketChannel;
        Selector selector;
        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }

        if(isRdb) ShuaiServer.loadRdbFile();
        if(isAof) ShuaiServer.loadAofFile();

        while(true) {
            try{
                selector.selectNow();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while(iterator.hasNext()) {

                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if(key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector,SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        ByteBuffer input = ByteBuffer.allocate(2000);
                        input.put(ShuaiConstants.WELCOME);
                        input.rewind();
                        key2.attach(input);
//                        while(!client.finishConnect());
                        client.write(input);
                        input.compact();
                    }else if(key.isReadable()) {
                        executor.submit(new ShuaiTask(key));
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (IOException e) {
                    key.cancel();
                    try{
                        key.channel().close();
                    }catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

        }

    }

    public static void loadRdbFile() {
        File rdbFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.RDB_SUFFIX);
        if(!rdbFile.exists()) return;
        ShuaiServer.rRdbFile.lock();
        try(
                FileInputStream fileInputStream = new FileInputStream(rdbFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        ){
            ShuaiServer.dbs = (ConcurrentLinkedDeque<ShuaiDB>) objectInputStream.readObject();
        } catch (Exception e){
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.RDB_LOAD_FAIL).speakOut();
        }finally {
            ShuaiServer.rRdbFile.unlock();
        }
    }

    public static void loadAofFile() {
        File aofFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.AOF_SUFFIX);
        if(!aofFile.exists()) return ;
        ShuaiServer.rAofFile.lock();
        try(
                FileReader fileReader = new FileReader(aofFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ){
            bufferedReader.lines().forEach(x -> {
                try{
                    executor.submit(new ShuaiTask(new ShuaiRequest(x,true)));
                }catch (RuntimeException ignored) {}
            });
        }catch (Exception e) {
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.AOF_LOAD_FAIL).speakOut();
        }finally {
            ShuaiServer.rAofFile.unlock();
        }
    }
}
