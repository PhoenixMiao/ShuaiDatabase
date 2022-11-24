//package com.phoenix.shuaidatabase.raft;
//
//import com.baidu.brpc.client.BrpcProxy;
//import com.baidu.brpc.client.RpcClient;
//import com.googlecode.protobuf.format.JsonFormat;
//import com.phoenix.shuaidatabase.single.ShuaiRequest;
//
///**
// * Created by wenweihu86 on 2017/5/14.
// */
//public class ClientMain {
//    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.printf("Usage: ./run_client.sh CLUSTER KEY [VALUE]\n");
//            System.exit(-1);
//        }
//
//        // parse args
//        String ipPorts = args[0];
//        String key = args[1];
//        String value = null;
//        if (args.length > 2) {
//            value = args[2];
//        }
//
//        // init rpc client
//        RpcClient rpcClient = new RpcClient(ipPorts);
//        ShuaiService exampleService = BrpcProxy.getProxy(rpcClient, ShuaiService.class);
//        final JsonFormat jsonFormat = new JsonFormat();
//
//        // set
//        if (value != null) {
//            ShuaiRequest setRequest = ShuaiRequest.newBuilder()
//                    .setKey(key).setValue(value).build();
//            ExampleProto.SetResponse setResponse = exampleService.set(setRequest);
//            System.out.printf("set request, key=%s value=%s response=%s\n",
//                    key, value, jsonFormat.printToString(setResponse));
//        } else {
//            // get
//            ExampleProto.GetRequest getRequest = ExampleProto.GetRequest.newBuilder()
//                    .setKey(key).build();
//            ExampleProto.GetResponse getResponse = exampleService.get(getRequest);
//            System.out.printf("get request, key=%s, response=%s\n",
//                    key, jsonFormat.printToString(getResponse));
//        }
//
//        rpcClient.stop();
//    }
//}