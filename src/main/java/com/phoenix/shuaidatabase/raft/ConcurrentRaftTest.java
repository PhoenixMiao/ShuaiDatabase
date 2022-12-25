package com.phoenix.shuaidatabase.raft;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.phoenix.shuaidatabase.raft.service.ShuaiService;
import com.phoenix.shuaidatabase.raft.service.impl.ShuaiServiceImpl;
import com.phoenix.shuaidatabase.single.ShuaiReply;
import com.phoenix.shuaidatabase.single.ShuaiRequest;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentRaftTest {

    public static void main(String[] args) {

        // parse args
        String ipPorts = "list://127.0.0.1:8001,127.0.0.1:8002,127.0.0.1:8003";
        RpcClient rpcClient = new RpcClient(ipPorts);
        ShuaiService exampleService = BrpcProxy.getProxy(rpcClient, ShuaiService.class);

        ExecutorService readThreadPool = Executors.newFixedThreadPool(10);
        ExecutorService writeThreadPool = Executors.newFixedThreadPool(10);
        Future<?>[] future = new Future[10];
        for (int i = 0; i < 10; i++) {
            future[i] = writeThreadPool.submit(new SetTask(exampleService, readThreadPool));
        }
    }

    public static class SetTask implements Runnable {
        private ShuaiService shuaiService;
        ExecutorService readThreadPool;

        public SetTask(ShuaiService exampleService, ExecutorService readThreadPool) {
            this.shuaiService = exampleService;
            this.readThreadPool = readThreadPool;
        }

        @Override
        public void run() {
            try{
                while (true) {
                    String key = UUID.randomUUID().toString();
                    String value = UUID.randomUUID().toString();
                    String input = "SET " + key + " " + value;
                    ShuaiRequest setRequest = new ShuaiRequest(input,ShuaiRequest.isValid(input));

                    long startTime = System.currentTimeMillis();
                    ShuaiReply setResponse = shuaiService.set(setRequest);
                    try {
                        if (setResponse != null) {
                            System.out.printf("set request, key=%s, value=%s, response=%s, elapseMS=%d\n",
                                    key, value,setResponse, System.currentTimeMillis() - startTime);
                            readThreadPool.submit(new GetTask(shuaiService, key));
                        } else {
                            System.out.printf("set request failed, key=%s value=%s\n", key, value);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class GetTask implements Runnable {
        private ShuaiService exampleService;
        private String key;

        public GetTask(ShuaiService exampleService, String key) {
            this.exampleService = exampleService;
            this.key = key;
        }

        @Override
        public void run() {
            String input = "GET " + key;
            ShuaiRequest getRequest = new ShuaiRequest(input,ShuaiRequest.isValid(input));
            long startTime = System.currentTimeMillis();
            ShuaiReply getResponse = exampleService.get(getRequest);
            try {
                if (getResponse != null) {
                    System.out.printf("get request, key=%s, response=%s, elapseMS=%d\n",
                            key, getResponse, System.currentTimeMillis() - startTime);
                } else {
                    System.out.printf("get request failed, key=%s\n", key);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
