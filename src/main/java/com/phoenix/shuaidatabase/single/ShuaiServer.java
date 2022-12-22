package com.phoenix.shuaidatabase.single;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ShuaiServer {

    public static int DEFAULT_PORT = 8888;

    public static ExecutorService executor = Executors.newFixedThreadPool(1000);

    public static ExecutorService fileExecutor = Executors.newSingleThreadExecutor();

    public static ScheduledExecutorService serverCronExecutor = Executors.newScheduledThreadPool(1);

    public static ShuaiEliminateStrategy eliminateStrategy = ShuaiEliminateStrategy.ALLKEYS_LRU;

    public static ExecutorService aofRewriteExecutor = Executors.newFixedThreadPool(10);

    public static volatile boolean reachLimitation = false;

    public static ConcurrentLinkedDeque<ShuaiDB> dbs = new ConcurrentLinkedDeque<ShuaiDB>(){{
        add(new ShuaiDB());
        add(new ShuaiDB());
        add(new ShuaiDB());
    }};

    public static ShuaiDB dbActive = dbs.getFirst();

    public static Boolean isAof = false;
    public static Boolean isRdb = false;
    public static Boolean isLsm = false;

    public static AtomicLong lastSave = new AtomicLong(System.currentTimeMillis());

    public static List<List<Integer>> saveParams = new LinkedList<List<Integer>>(){{
        add(new ArrayList<>(Arrays.asList(900,100,0)));
        add(new ArrayList<>(Arrays.asList(300,1000,0)));
        add(new ArrayList<>(Arrays.asList(60,10000,0)));
        add(new ArrayList<>(Arrays.asList(10,5,0)));
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

    static final ReentrantReadWriteLock lsmLock = new ReentrantReadWriteLock();

    static final Lock rLsmFile = lsmLock.readLock();
    static final Lock wLsmFile = lsmLock.writeLock();

    static final ReentrantLock rbTreeLock = new ReentrantLock();

    static final Condition rbTreeCondition = rbTreeLock.newCondition();

//    static final ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock();
//
//    static final Lock modifyDBLock = dbLock.readLock();
//    static final Lock switchDBLock = dbLock.writeLock();

    static volatile AtomicLong availableMemory = new AtomicLong(ShuaiConstants.MAX_MEMORY);

    public static void open() {
        //for initialize
        ShuaiCommand shuaiCommand;
        ShuaiServer shuaiServer;

        if(isRdb) ShuaiServer.loadRdbFile();
        if(isAof) ShuaiServer.loadAofFile();

        Iterator<ShuaiDB> it = ShuaiServer.dbs.iterator();
        ShuaiDB db;
        try{
            for(int i = 0;i<ShuaiServer.dbs.size();i++) {
                db = it.next();
                RollExpires rollExpires = new RollExpires(db);
                Thread thread = new Thread(rollExpires);
                thread.setDaemon(true);
                thread.start();
                if(!it.hasNext()) break;
            }
        }catch (Exception e){
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.FAIL_FAST).speakOut();
        }

        serverCronExecutor.scheduleAtFixedRate(new ServerCron(),1000,1000,TimeUnit.MILLISECONDS);
    }

    public static void polling() {
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

        System.out.println("Listening for connections on port 8888");

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
                        ByteBuffer input = ByteBuffer.allocate(10240);
                        input.put(ShuaiConstants.WELCOME);
                        input.flip();
                        key2.attach(input);
//                        while(!client.finishConnect());
                        client.write(input);
                        input.clear();
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

    public static void main(String[] args) {
        open();
        polling();
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
            dbs.forEach(ShuaiDB::initExpires);
        } catch (Exception e){
            e.printStackTrace();
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.RDB_LOAD_FAIL).speakOut();
        }finally {
            ShuaiServer.rRdbFile.unlock();
        }
    }

    public static void loadAofFile() {
        File aofFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.AOF_SUFFIX);
        if(!aofFile.exists()) {
            System.out.println("can not find");
            return ;
        }
        ShuaiServer.rAofFile.lock();
        try(
                FileReader fileReader = new FileReader(aofFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ){
            bufferedReader.lines().forEach(x -> {
                try{
                    fileExecutor.submit(new ShuaiTask(new ShuaiRequest(x,true)));
                }catch (RuntimeException ignored) {}
            });
        }catch (Exception e) {
            new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.AOF_LOAD_FAIL).speakOut();
        }finally {
            ShuaiServer.rAofFile.unlock();
        }
    }

    static class LSMWrite implements Runnable {
        private final ShuaiDB db;

        public LSMWrite(ShuaiDB db) {
            this.db = db;
        }

        public ShuaiDB getDb() {
            return db;
        }

        @Override
        public void run() {
            int size = db.getDict().size();
            rbTreeLock.lock();
            try {
                while (db.getDict().size() >= size / 2) {
                    ShuaiEntry entry = db.allKeysLRU();
                    if (entry != null) db.getLsmTree().rbInsert(new ShuaiRedBlackTree.Node(entry, false,db.getLsmTree().getNil()));
                    else rbTreeCondition.awaitNanos(3 * ShuaiConstants.ONT_NANO);
                }
            } catch (InterruptedException e) {
                new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.LSM_THREAD_INTERRUPTED);
            } finally {
                rbTreeLock.unlock();
            }
            if (db.getLsmTree().getHeight() > 0) {
                List<ShuaiEntry> list = new LinkedList<>();
                while (db.getLsmTree().getHeight() >= 4) list.add(db.getLsmTree().deleteRoot().getEntry());
                //todo place it in distribute system
                File lsmFolder = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.LSM_SUFFIX
                        + "\\db" + db.getId() + "\\");
                lsmFolder.mkdir();
                File lsmFile = new File(lsmFolder.getPath() +"\\chunk"+ ShuaiDB.lsmID.incrementAndGet());
                ShuaiServer.wLsmFile.lock();
                try (
                        FileOutputStream fileStream = new FileOutputStream(lsmFile);
                        ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
                ) {
                    if (lsmFile.exists()) lsmFile.delete();
                    lsmFile.createNewFile();
                    objectStream.writeObject(list);
                } catch (Exception e) {
                    e.printStackTrace();
                    new ShuaiReply(ShuaiReplyStatus.INNER_FAULT, ShuaiErrorCode.LSM_WRITE_FAIL).speakOut();
                } finally {
                    ShuaiServer.wLsmFile.unlock();
                }

                File aofFile = new File(ShuaiConstants.PERSISTENCE_PATH + ShuaiConstants.AOF_SUFFIX);
                if(aofFile.exists()) {
                    ShuaiServer.wAofFile.lock();
                    try{
                        aofFile.delete();
                    }finally {
                        ShuaiServer.wAofFile.unlock();
                    }
                }
            }
        }
    }

    static class ServerCron implements Runnable {

        @Override
        public void run() {
            try {
                //eliminate
                ShuaiServer.availableMemory.addAndGet(-(Runtime.getRuntime().totalMemory()) - Runtime.getRuntime().freeMemory());
                long free = ShuaiServer.availableMemory.get();
                for (ShuaiDB db : dbs) {
                    if (free < 1024 * 1024) {
                        reachLimitation = true;
                        switch (eliminateStrategy) {
                            case ALLKEYS_LRU:
                                db.allKeysLRU();
                            case VOLATILE_LRU:
                                db.volatileKeysLRU();
                            case ALLKEYS_RANDOM:
                                db.allKeysRandom();
                            case VOLATILE_RANDOM:
                                db.volatileKeysRandom();
                            default:
                        }
                    } else if (ShuaiServer.eliminateStrategy == ShuaiEliminateStrategy.LSM_TREE &&
                            free < ShuaiConstants.MAX_MEMORY / 100) {
                        fileExecutor.submit(new LSMWrite(db));
                    }
                }

                //expire
                for (ShuaiDB db : dbs) {
                    db.getExLock().lock();
                    try {
                        db.getCondition().signalAll();
                    } finally {
                        db.getExLock().unlock();
                    }
                }

                //produce rdb file
                if(isRdb) new ShuaiTask.RdbProduce(true,false).call();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class RollExpires implements Runnable {

        private final ShuaiDB db;

        public RollExpires(ShuaiDB db) {
            this.db = db;
        }

        @Override
        public void run() {
            while (true) {
                db.getExLock().lock();
                try {
                    DelayQueue<ShuaiExpireKey> delayQueue = db.getExpires();
                    ShuaiExpireKey key = null;
                    while (delayQueue.isEmpty() || (key = delayQueue.poll()) == null) db.getCondition().await();
                    db.getDict().remove(key.getKey());
                    String input = "DEL " + (key.getKey()).toString();
                    if(ShuaiServer.isAof) executor.submit(new ShuaiTask.AppendOnlyFile(new ShuaiRequest(input,ShuaiRequest.isValid(input))));
                } catch (Exception e) {
                    e.printStackTrace();
                    new ShuaiReply(ShuaiReplyStatus.INNER_FAULT, ShuaiErrorCode.EXPIRE_THREAD_INTERRUPTED).speakOut();
                } finally {
                    db.getExLock().unlock();
                }
            }
        }
    }

}
