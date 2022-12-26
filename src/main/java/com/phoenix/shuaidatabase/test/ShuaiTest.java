package com.phoenix.shuaidatabase.test;

import com.phoenix.shuaidatabase.single.ShuaiRequest;
import com.phoenix.shuaidatabase.single.ShuaiServer;
import com.phoenix.shuaidatabase.single.ShuaiTask;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ShuaiTest {
    private static final Integer DEFAULT_PORT = 8888;
    private static final String STATUS = "local1000TFT";
    private static final Integer KEYLENGTH = 12;
    private static final Integer TIMES = 1000;
    private static final String DEFAULT_SHUAI_IP = "localhost";
    private static final String DEFAULT_REDIS_IP = "localhost";

    public static final ShuaiTask task = new ShuaiTask();

    private static long serialCommandTestJedis(String command, String[] args, Integer times) {
        Jedis jedis = new Jedis(DEFAULT_REDIS_IP, 6379);
        Connection client = jedis.getClient();
        //开始时间
        long startTime = System.currentTimeMillis();

        //发送命令
        for (int i = 0; i < times; i++) {
            client.sendCommand(() -> SafeEncoder.encode(command.toLowerCase(Locale.ROOT)), args);
            while (client.getOne() == null) ;
        }
        long endTime = System.currentTimeMillis();
        client.close();
        jedis.close();
        return endTime - startTime;
    }

    private static long serialCommandTestExecutor(String command, String[] args, Integer times) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        String input = builder.toString();
        ShuaiRequest request = new ShuaiRequest(input,false);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < times; i++) {
            task.executeMethod(request);
//            reply.speakOut();
        }

        //测量时间
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    private static long serialCommandTestShuai(String command, String[] args, Integer times) throws Exception {
        //我们的数据库需要把命令和参数拼在一起，所以拼接参数
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        SocketChannel client = SocketChannel.open(new InetSocketAddress(DEFAULT_SHUAI_IP, DEFAULT_PORT));
        List<ByteBuffer> bufferList = new ArrayList<>(times * 2);
        for (int i = 0; i < times * 2; i++) bufferList.add(ByteBuffer.allocate(500));
        ByteBuffer buffer = ByteBuffer.allocate(500);
        client.read(buffer);
        String input = builder.toString();
        long startTime = System.currentTimeMillis();


        for (int i = 0; i < times * 2; i += 2) {
            ByteBuffer buffer1 = bufferList.get(i);
            buffer1.put(input.getBytes(StandardCharsets.UTF_8));
            buffer1.flip();
            client.write(buffer1);
            ByteBuffer buffer2 = bufferList.get(i + 1);
            client.read(buffer2);
        }

        //测量时间
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    private static long concurrentCommandTestJedis(String command, String[] args, Integer threadNum) throws Exception {
        //开始信号量，所有线程都开始
        final CountDownLatch startSignal = new CountDownLatch(1);
        //结束信号量
        final CountDownLatch doneSignal = new CountDownLatch(threadNum);
        ConcurrentLinkedQueue<Jedis> jedisConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < threadNum; i++) {
            jedisConcurrentLinkedQueue.add(new Jedis(DEFAULT_REDIS_IP, 6379));
        }
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            threadPoolExecutor.execute(new Thread(() -> {
                try {
                    Jedis jedis = jedisConcurrentLinkedQueue.poll();
                    Connection client = jedis.getClient();
                    startSignal.await();
                    //测试主体
                    client.sendCommand(() -> SafeEncoder.encode(command), args);
                    while (client.getOne() == null) ;
                    doneSignal.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        long startTime = System.currentTimeMillis();
        startSignal.countDown();
        doneSignal.await();
        long endTime = System.currentTimeMillis();
        return (endTime - startTime);
    }

    private static long concurrentCommandTestShuai(String command, String[] args, Integer threadNum) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        String arguments = builder.toString();
        //开始信号量
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(threadNum);
        final CountDownLatch readySignal = new CountDownLatch(threadNum);

        ConcurrentLinkedQueue<TestUnit> clients = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<TestUnit> clients2 = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadNum; i++) {
            SocketChannel client = SocketChannel.open(new InetSocketAddress(DEFAULT_SHUAI_IP, DEFAULT_PORT));
            clients.add(new TestUnit(client, ByteBuffer.allocate(500), ByteBuffer.allocate(500)));
            ByteBuffer buffer = ByteBuffer.allocate(500);
            client.read(buffer);
        }

        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            threadPoolExecutor.execute(new Thread(() -> {
                try {
                    //测试主体
                    TestUnit testUnit = clients.poll();
                    testUnit.getBuffer1().put(arguments.getBytes(StandardCharsets.UTF_8));
                    testUnit.getBuffer1().flip();
                    readySignal.countDown();
                    startSignal.await();
                    testUnit.getClient().write(testUnit.getBuffer1());
                    testUnit.getClient().read(testUnit.getBuffer2());
                    doneSignal.countDown();
                    clients2.add(testUnit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        readySignal.await();
        long startTime = System.currentTimeMillis();
        startSignal.countDown();
        doneSignal.await();
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);
        return time;
    }

    private static long concurrentCommandTestExecutor(String command, String[] args, Integer threadNum) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        String input = builder.toString();
        //开始信号量
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(threadNum);

        ShuaiServer shuaiServer = null;
        ShuaiRequest request = new ShuaiRequest(input,false);
        for (int i = 0; i < 10; i++) task.executeMethod(request);
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            threadPoolExecutor.execute(new Thread(() -> {
                try {
                    startSignal.await();
                    task.executeMethod(request);
                    doneSignal.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        long startTime = System.currentTimeMillis();
        startSignal.countDown();
        doneSignal.await();
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);
        return time;
    }

    //串行对比测试
    private static void serialCommandTest(String command, String[] args, Integer times) throws Exception {
        long time1 = serialCommandTestJedis(command, args, times);
        System.out.println("Jedis Serial Test: command:" + command + " time:" + time1 + "ms");
        long time2 = serialCommandTestShuai(command, args, times);
        System.out.println("Shuai Serial Test: command:" + command + " time:" + time2 + "ms");
        long time3 = serialCommandTestExecutor(command, args, times);
        System.out.println("Shuai Executor Serial Test: command:" + command + " time:" + time3 + "ms");
        ExcelOutput excelOutput = new ExcelOutput("serial" + STATUS + KEYLENGTH);
        String[] results = {command, time1 + "ms", time2 + "ms", time3 + "ms"};
        excelOutput.writeExcel(results);
    }

    //并行对比测试
    private static void concurrentCommandTest(String command, String[] args, Integer threadNum) throws Exception {
        long time1 = concurrentCommandTestJedis(command, args, threadNum);
        System.out.println("Jedis Concurrent Test: command:" + command + " time:" + time1 + "ms");
        long time2 = concurrentCommandTestShuai(command, args, threadNum);
        System.out.println("Shuai Concurrent Test: command:" + command + " time:" + time2 + "ms");
//        long time3 = concurrentCommandTestExecutor(command, args, threadNum);
//        System.out.println("Shuai Executor Concurrent Test: command:" + command + " time:" + time3 + "ms");
        ExcelOutput excelOutput = new ExcelOutput(STATUS);
        String[] results = {command, time1 + "ms", time2 + "ms"};
        excelOutput.writeExcel(results);
    }


    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        //        字符串类数据
        String[] arg1 = new String[2];
        String[] arg2 = new String[1];
        String[] arg3 = new String[3];
        String[] arg4 = new String[3];
        arg1[0] = arg2[0] = arg3[0] = arg4[0] =getRandomString(12);
        arg1[1] = getRandomString(KEYLENGTH);

        int offset = random.nextInt(KEYLENGTH);
        int end = random.nextInt(KEYLENGTH);
        if(offset > end){
            int tmp = offset;
            offset = end;
            end = tmp;
        }
        arg3[1] = arg4[1] = String.valueOf(offset);
        arg3[2] = getRandomString(KEYLENGTH);
        arg4[2] = String.valueOf(end);
//        serialCommandTest("SET", arg1, TIMES);
//        serialCommandTest("GET", arg2, TIMES);
//        serialCommandTest("SETRANGE",arg3, TIMES);
//        serialCommandTest("GETRANGE",arg4, TIMES);
//        concurrentCommandTest("SET", arg1, TIMES);
//        concurrentCommandTest("GET", arg2, TIMES);
//        concurrentCommandTest("SETRANGE",arg3, TIMES);
//        concurrentCommandTest("GETRANGE",arg4, TIMES);

        //集合
        String[] setArg1 = new String[2];
        String[] setArg2 = new String[1];
        String[] setArg3 = new String[2];
        String[] setArg4 = new String[1];
        String[] setArg5 = new String[2];
        setArg1[0] = setArg2[0] = setArg3[0] = getRandomString(KEYLENGTH);
        setArg1[1] = getRandomString(KEYLENGTH);
////        serialCommandTest("SADD",setArg1,TIMES);
        setArg4[0] = setArg5[0] = setArg1[0] = setArg3[1] = getRandomString(KEYLENGTH);
//        serialCommandTest("SADD",setArg1,TIMES);
//        serialCommandTest("SCARD",setArg2,TIMES);
//        serialCommandTest("SDIFF",setArg3,TIMES);
//        serialCommandTest("SINTER",setArg3,TIMES);
//        serialCommandTest("SMEMBERS",setArg4,TIMES);
        setArg5[1] = "1";

//        concurrentCommandTest("SADD",setArg1,TIMES);
//        concurrentCommandTest("SCARD",setArg2,TIMES);
//        setArg4[0] = setArg5[0] = setArg1[0] = setArg3[1] = getRandomString(KEYLENGTH);
//        concurrentCommandTest("SDIFF",setArg3,TIMES);
//        concurrentCommandTest("SINTER",setArg3,TIMES);
//        concurrentCommandTest("SMEMBERS",setArg4,TIMES);
//        concurrentCommandTest("SRANDMEMBER",setArg5,TIMES);

        //列表
        String[] listArg1 = new String[2];
        String[] listArg2 = new String[1];
        String[] listArg3 = new String[3];

        listArg1[0] = listArg2[0] = listArg3[0]= getRandomString(KEYLENGTH);
        listArg1[1] = getRandomString(KEYLENGTH);
        listArg3[1] = "0";
        listArg3[2] = "50";
//        serialCommandTest("LPUSH",listArg1,TIMES);
//        serialCommandTest("RPUSH",listArg1,TIMES);
//        serialCommandTest("LLEN",listArg2,TIMES);
//        serialCommandTest("LRANGE",listArg3,TIMES);
//        serialCommandTest("LPOP",listArg2,TIMES);
//        serialCommandTest("RPOP",listArg2,TIMES);
//
//        concurrentCommandTest("LPUSH",listArg1,TIMES);
//        concurrentCommandTest("RPUSH",listArg1,TIMES);
//        concurrentCommandTest("LRANGE",listArg3,TIMES);
//        concurrentCommandTest("LLEN",listArg2,TIMES);
//
//        concurrentCommandTest("LPOP",listArg2,TIMES);
//        concurrentCommandTest("RPOP",listArg2,TIMES);

        //hashSet
        String[] setArg = new String[3];
        String[] getArg = new String[2];
        String[] getAllArg = new String[1];

        setArg[0] = getArg[0] = getAllArg[0] = getRandomString(KEYLENGTH);
        setArg[1] = getRandomString(KEYLENGTH);
        setArg[2] = getRandomString(KEYLENGTH);
        getArg[1] = setArg[1];
//        serialCommandTest("HSET",setArg,TIMES);
//        serialCommandTest("HGET",getArg,TIMES);
//        serialCommandTest("HGETALL",getAllArg,TIMES);
//        serialCommandTest("HLEN",getAllArg,TIMES);

        concurrentCommandTest("HSET", setArg, TIMES);
        concurrentCommandTest("HGET", getArg, TIMES);
        concurrentCommandTest("HGETALL", getAllArg, TIMES);
        concurrentCommandTest("HLEN",getAllArg,TIMES);

        //zSet
        String[] addArg = new String[3];
        String[] countArg = new String[3];
        String[] cardArg = new String[1];

        addArg[0] = countArg[0] = cardArg[0] = getRandomString(KEYLENGTH);
//        Random random = new Random();
        addArg[1] = random.nextInt(100) + "";
        addArg[2] = getRandomString(KEYLENGTH);


        countArg[1] = "0";
        countArg[2] = "100";
//        serialCommandTest("ZADD", addArg, TIMES);
//        serialCommandTest("ZCOUNT", countArg, TIMES);
//        serialCommandTest("ZCARD", cardArg, TIMES);
//        serialCommandTest("ZRANGE", countArg, TIMES);


        concurrentCommandTest("ZADD", addArg, TIMES);
        concurrentCommandTest("ZCOUNT", countArg, TIMES);
        concurrentCommandTest("ZCARD", cardArg, TIMES);
        concurrentCommandTest("ZRANGE", countArg, TIMES);


    }
}
