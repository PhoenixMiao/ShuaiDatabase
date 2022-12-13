package com.phoenix.shuaidatabase.test;

import com.phoenix.shuaidatabase.single.ShuaiRequest;
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
import java.util.concurrent.*;

public class ShuaiTest {
    public static final Integer DEFAULT_PORT = 8888;

    private static long serialCommandTestJedis(String command, String[] args, Integer times) {
        Jedis jedis = new Jedis("localhost", 6379);
        Connection client = jedis.getClient();
        //开始时间
        long startTime = System.currentTimeMillis();

        //发送命令
        for (int i = 0; i < times; i++) {
            client.sendCommand(() -> SafeEncoder.encode(command.toLowerCase(Locale.ROOT)), args);
            while (client.getBulkReply() == null) ;
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long serialCommandTestExecutor(String command, String[] args, Integer times) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        String input = builder.toString();
        ShuaiRequest request = new ShuaiRequest(input,ShuaiRequest.isValid(input));

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < times; i++) {
            new ShuaiTask(request).executeMethod(request);
        }

        //测量时间
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);

        return time;
    }

    private static long serialCommandTestShuai(String command, String[] args, Integer times) throws Exception {
        //我们的数据库需要把命令和参数拼在一起，所以拼接参数
        StringBuilder builder = new StringBuilder();
        builder.append(command.toUpperCase(Locale.ROOT)).append(" ");
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        SocketChannel client = SocketChannel.open(new InetSocketAddress(DEFAULT_PORT));
        List<ByteBuffer> bufferList = new ArrayList<>(times * 2);
        for (int i = 0; i < times * 2; i++) bufferList.add(ByteBuffer.allocate(2000));
        ByteBuffer buffer = ByteBuffer.allocate(2000);
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

        return (endTime - startTime);
    }

    private static long concurrentCommandTestJedis(String command, String[] args, Integer threadNum) throws Exception {
        //开始信号量，所有线程都开始
        final CountDownLatch startSignal = new CountDownLatch(1);
        //结束信号量
        final CountDownLatch doneSignal = new CountDownLatch(threadNum);
        ConcurrentLinkedQueue<Jedis> jedisConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < threadNum; i++) {
            jedisConcurrentLinkedQueue.add(new Jedis("localhost",6379));
        }
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            threadPoolExecutor.execute(new Thread(() -> {
                try  {
                    Jedis jedis = jedisConcurrentLinkedQueue.poll();
                    Connection client = jedis.getClient();
                    startSignal.await();
                    //测试主体
                    client.sendCommand(() -> SafeEncoder.encode(command), args);
                    String reply = "";
                    while (reply.equals("")) reply = client.getBulkReply().trim();
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
            SocketChannel client = SocketChannel.open(new InetSocketAddress(DEFAULT_PORT));
            clients.add(new TestUnit(client, ByteBuffer.allocate(2000), ByteBuffer.allocate(2000)));
            ByteBuffer buffer = ByteBuffer.allocate(2000);
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

        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            threadPoolExecutor.execute(new Thread(() -> {
                try {
                    ShuaiRequest request = new ShuaiRequest(input,ShuaiRequest.isValid(input));
                    startSignal.await();
                    new ShuaiTask(request).executeMethod(request);
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
        long time2 = serialCommandTestShuai(command, args, times);
        long time3 = serialCommandTestExecutor(command, args, times);
        System.out.println("Jedis Serial Test: command:" + command + " time:" + time1 + "ms");
        System.out.println("Shuai Serial Test: command:" + command + " time:" + time2 + "ms");
        System.out.println("Shuai Executor Serial Test: command:" + command + " time:" + time3 + "ms");
    }

    //并行对比测试
    private static void concurrentCommandTest(String command, String[] args, Integer threadNum) throws Exception {
        long time1 = concurrentCommandTestJedis(command, args, threadNum);
        long time2 = concurrentCommandTestShuai(command, args, threadNum);
        long time3 = concurrentCommandTestExecutor(command, args, threadNum);
        System.out.println("Jedis Concurrent Test: command:" + command + " time:" + time1 + "ms");
        System.out.println("Shuai Concurrent Test: command:" + command + " time:" + time2 + "ms");
        System.out.println("Shuai Executor Concurrent Test: command:" + command + " time:" + time3 + "ms");
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
        String[] arguments = new String[3];
        arguments[0] = "r";
        arguments[1] = "b";
        arguments[2] = "c";
        String[] arg2 = new String[2];
        arg2[0] = "s";
        arg2[1] = "b";
//        serialCommandTestJedis("set",arguments,1);
//        serialCommandTest("set",arg2,10000);
        concurrentCommandTest("set", arg2, 5000);
//        concurrentCommandTest("hmset", arguments, 10000);
//        concurrentCommandTestJedis("get", arg2, 10000,10000);
//        serialCommandTestShuai("set", arguments, 10000);
//        serialCommandTestShuai("get", arg2, 100000);

//        serialCommandTest("hmset", arguments,10000);
    }
}
