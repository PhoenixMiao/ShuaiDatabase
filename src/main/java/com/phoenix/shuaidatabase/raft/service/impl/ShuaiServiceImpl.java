package com.phoenix.shuaidatabase.raft.service.impl;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.instance.Endpoint;
import com.phoenix.shuaidatabase.raft.Peer;
import com.phoenix.shuaidatabase.raft.RaftNode;
import com.phoenix.shuaidatabase.raft.ShuaiStateMachine;
import com.phoenix.shuaidatabase.raft.proto.RaftProto;
import com.phoenix.shuaidatabase.raft.service.ShuaiService;
import com.phoenix.shuaidatabase.single.*;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShuaiServiceImpl implements ShuaiService {

    private RaftNode raftNode;
    private ShuaiStateMachine stateMachine;
    private int leaderId = -1;
    private RpcClient leaderRpcClient = null;
    private Lock leaderLock = new ReentrantLock();

    public ShuaiServiceImpl(RaftNode raftNode, ShuaiStateMachine stateMachine) {
        this.raftNode = raftNode;
        this.stateMachine = stateMachine;
    }

    private void onLeaderChangeEvent() {
        if (raftNode.getLeaderId() != -1
                && raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()
                && leaderId != raftNode.getLeaderId()) {
            leaderLock.lock();
            if (leaderId != -1 && leaderRpcClient != null) {
                leaderRpcClient.stop();
                leaderRpcClient = null;
                leaderId = -1;
            }
            leaderId = raftNode.getLeaderId();
            Peer peer = raftNode.getPeerMap().get(leaderId);
            Endpoint endpoint = new Endpoint(peer.getServer().getEndpoint().getHost(),
                    peer.getServer().getEndpoint().getPort());
            RpcClientOptions rpcClientOptions = new RpcClientOptions();
            rpcClientOptions.setGlobalThreadPoolSharing(true);
            leaderRpcClient = new RpcClient(endpoint, rpcClientOptions);
            leaderLock.unlock();
        }
    }

    public ShuaiReply set(ShuaiRequest request) throws IOException {
        // 如果自己不是leader，将写请求转发给leader
        if (raftNode.getLeaderId() <= 0) {
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT, ShuaiErrorCode.CAN_NOT_GET_LEADER);
        } else if (raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()) {
            onLeaderChangeEvent();
            ShuaiService shuaiService = BrpcProxy.getProxy(leaderRpcClient, ShuaiService.class);
            return shuaiService.set(request);
        } else {
            // 数据同步写入raft集群
            byte[] data = request.toBytes();
            boolean success = raftNode.replicate(data, RaftProto.EntryType.ENTRY_TYPE_DATA);
            if(success) {
                return new ShuaiReply(ShuaiReplyStatus.OK);
            } else return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT,ShuaiErrorCode.RAFT_FAIL);
        }
    }

    public ShuaiReply get(ShuaiRequest request) {
        return new ShuaiTask().executeMethod(request);
    }
}
