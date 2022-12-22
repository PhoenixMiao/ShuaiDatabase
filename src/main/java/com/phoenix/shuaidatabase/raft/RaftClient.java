package com.phoenix.shuaidatabase.raft;import com.baidu.brpc.client.BrpcProxy;import com.baidu.brpc.client.RpcClient;import com.googlecode.protobuf.format.JsonFormat;import com.phoenix.shuaidatabase.raft.service.ShuaiService;import com.phoenix.shuaidatabase.raft.service.impl.RaftConsensusServiceImpl;import com.phoenix.shuaidatabase.raft.service.impl.ShuaiServiceImpl;import com.phoenix.shuaidatabase.single.ShuaiCommand;import com.phoenix.shuaidatabase.single.ShuaiRequest;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.util.Scanner;import java.util.concurrent.ExecutionException;import java.util.concurrent.FutureTask;public class RaftClient {    public static ShuaiService shuaiService;    public static RpcClient rpcClient;    public static Boolean initClient() {        String ipPorts = "list://127.0.0.1:8001,127.0.0.1:8002,127.0.0.1:8003";        rpcClient = new RpcClient(ipPorts);        shuaiService = BrpcProxy.getProxy(rpcClient, ShuaiService.class);        return true;    }    public static void main(String[] args) throws InterruptedException, ExecutionException {        FutureTask<Boolean> future = new FutureTask<>(RaftClient::initClient);        new Thread(future).start();        future.get();        System.out.println("请输入命令");        try{            while (true) {                Scanner scanner = new Scanner(System.in);                String input = scanner.nextLine();                if(input.equals("quit")) break;                ShuaiRequest request = new ShuaiRequest(input,ShuaiRequest.isValid(input));                if(ShuaiCommand.commands.get(input.split(" ")[0]).isWillModify()) shuaiService.set(request).speakOut();                else shuaiService.get(request).speakOut();            }        }catch (Exception e) {            e.printStackTrace();        }finally {            rpcClient.stop();        }    }}