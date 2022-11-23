//package com.phoenix.shuaidatabase.raft;
//
//import com.baidu.brpc.client.BrpcProxy;
//import com.baidu.brpc.client.RpcClient;
//import com.github.wenweihu86.raft.example.server.service.ExampleProto;
//import com.github.wenweihu86.raft.example.server.service.ShuaiService;
//import com.googlecode.protobuf.format.JsonFormat;
//
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
///**
// * Created by wenweihu86 on 2017/5/14.
// */
//public class ConcurrentClientMain {
//    private static JsonFormat jsonFormat = new JsonFormat();
//
//    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.out.printf("Usage: ./run_concurrent_client.sh THREAD_NUM\n");
//            System.exit(-1);
//        }
//
//        // parse args
//        String ipPorts = args[0];
//        RpcClient rpcClient = new RpcClient(ipPorts);
//        ShuaiService exampleService = BrpcProxy.getProxy(rpcClient, ShuaiService.class);
//
//        ExecutorService readThreadPool = Executors.newFixedThreadPool(3);
//        ExecutorService writeThreadPool = Executors.newFixedThreadPool(3);
//        Future<?>[] future = new Future[3];
//        for (int i = 0; i < 3; i++) {
//            future[i] = writeThreadPool.submit(new SetTask(exampleService, readThreadPool));
//        }
//    }
//
//    public static class SetTask implements Runnable {
//        private ShuaiService exampleService;
//        ExecutorService readThreadPool;
//
//        public SetTask(ShuaiService exampleService, ExecutorService readThreadPool) {
//            this.exampleService = exampleService;
//            this.readThreadPool = readThreadPool;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                String key = UUID.randomUUID().toString();
//                String value = UUID.randomUUID().toString();
//                ShuaiRequest setRequest = ShuaiRequest.newBuilder()
//                        .setKey(key).setValue(value).build();
//
//                long startTime = System.currentTimeMillis();
//                ExampleProto.SetResponse setResponse = exampleService.set(setRequest);
//                try {
//                    if (setResponse != null) {
//                        System.out.printf("set request, key=%s, value=%s, response=%s, elapseMS=%d\n",
//                                key, value, jsonFormat.printToString(setResponse), System.currentTimeMillis() - startTime);
//                        readThreadPool.submit(new GetTask(exampleService, key));
//                    } else {
//                        System.out.printf("set request failed, key=%s value=%s\n", key, value);
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public static class GetTask implements Runnable {
//        private ShuaiService exampleService;
//        private String key;
//
//        public GetTask(ShuaiService exampleService, String key) {
//            this.exampleService = exampleService;
//            this.key = key;
//        }
//
//        @Override
//        public void run() {
//            ExampleProto.GetRequest getRequest = ExampleProto.GetRequest.newBuilder()
//                    .setKey(key).build();
//            long startTime = System.currentTimeMillis();
//            ExampleProto.GetResponse getResponse = exampleService.get(getRequest);
//            try {
//                if (getResponse != null) {
//                    System.out.printf("get request, key=%s, response=%s, elapseMS=%d\n",
//                            key, jsonFormat.printToString(getResponse), System.currentTimeMillis() - startTime);
//                } else {
//                    System.out.printf("get request failed, key=%s\n", key);
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//}
