package com.phoenix.shuaidatabase.raft;

import com.baidu.brpc.server.RpcServer;
import com.phoenix.shuaidatabase.raft.proto.RaftProto;
import com.phoenix.shuaidatabase.raft.service.RaftClientService;
import com.phoenix.shuaidatabase.raft.service.RaftConsensusService;
import com.phoenix.shuaidatabase.raft.service.ShuaiService;
import com.phoenix.shuaidatabase.raft.service.impl.RaftClientServiceImpl;
import com.phoenix.shuaidatabase.raft.service.impl.RaftConsensusServiceImpl;
import com.phoenix.shuaidatabase.raft.service.impl.ShuaiServiceImpl;

import java.util.ArrayList;
import java.util.List;


public class ServerMain {
    public static void main(String[] args) {
        String[][] clusters = new String[][] {
                {"\\src\\main\\resources\\raft\\","127.0.0.1:8001:1,127.0.0.1:8002:2,127.0.0.1:8003:3","127.0.0.1:8001:1"},
                {"\\src\\main\\resources\\raft\\","127.0.0.1:8001:1,127.0.0.1:8002:2,127.0.0.1:8003:3","127.0.0.1:8002:2"},
                {"\\src\\main\\resources\\raft\\","127.0.0.1:8001:1,127.0.0.1:8002:2,127.0.0.1:8003:3","127.0.0.1:8003:3"}
        };
        if(args[0].equals("2")) args = clusters[1];
        else if(args[0].equals("3")) args = clusters[2];
        else args = clusters[0];
        // parse args
        // raft data dir
        String dataPath = args[0];
        // peers, format is "host:port:serverId,host2:port2:serverId2"
        String servers = args[1];
        String[] splitArray = servers.split(",");
        List<RaftProto.Server> serverList = new ArrayList<>();
        for (String serverString : splitArray) {
            RaftProto.Server server = parseServer(serverString);
            serverList.add(server);
        }
        // local server
        RaftProto.Server localServer = parseServer(args[2]);

        // 初始化RPCServer
        RpcServer server = new RpcServer(localServer.getEndpoint().getPort());
        // 设置Raft选项，比如：
        // just for test snapshot
        RaftOptions raftOptions = new RaftOptions();
        raftOptions.setDataDir(dataPath);
        raftOptions.setSnapshotMinLogSize(10 * 1024);
        raftOptions.setSnapshotPeriodSeconds(30);
        raftOptions.setMaxSegmentFileSize(1024 * 1024);
        // 应用状态机
        ShuaiStateMachine stateMachine = new ShuaiStateMachine();
        // 初始化RaftNode
        RaftNode raftNode = new RaftNode(raftOptions, serverList, localServer, stateMachine);
        // 注册Raft节点之间相互调用的服务
        RaftConsensusService raftConsensusService = new RaftConsensusServiceImpl(raftNode);
        server.registerService(raftConsensusService);
        // 注册给Client调用的Raft服务
        RaftClientService raftClientService = new RaftClientServiceImpl(raftNode);
        server.registerService(raftClientService);
        // 注册应用自己提供的服务
        ShuaiService exampleService = new ShuaiServiceImpl(raftNode, stateMachine);
        server.registerService(exampleService);
        // 启动RPCServer，初始化Raft节点
        server.start();
        raftNode.init();
    }

    private static RaftProto.Server parseServer(String serverString) {
        String[] splitServer = serverString.split(":");
        String host = splitServer[0];
        Integer port = Integer.parseInt(splitServer[1]);
        Integer serverId = Integer.parseInt(splitServer[2]);
        RaftProto.Endpoint endPoint = RaftProto.Endpoint.newBuilder()
                .setHost(host).setPort(port).build();
        RaftProto.Server.Builder serverBuilder = RaftProto.Server.newBuilder();
        RaftProto.Server server = serverBuilder.setServerId(serverId).setEndpoint(endPoint).build();
        return server;
    }
}
