package com.phoenix.shuaidatabase.raft;

import com.phoenix.shuaidatabase.single.*;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class ShuaiStateMachine implements StateMachine{

    private String raftDataDir;

    public ShuaiStateMachine(String raftDataDir) {
        this.raftDataDir = raftDataDir;
    }

    @Override
    public void writeSnapshot(String snapshotDir) {
        try{
            FileUtils.copyDirectory(new File(ShuaiConstants.PERSISTENCE_PATH),new File(snapshotDir));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void readSnapshot(String snapshotDir) {
        try{
            FileUtils.copyDirectory(new File(ShuaiConstants.PERSISTENCE_PATH),new File(snapshotDir));
            ShuaiServer.open();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void apply(byte[] dataBytes) {
        try{
            ShuaiRequest request = (ShuaiRequest) ShuaiTalk.backToObject(dataBytes);
            if(!request.isFake()) ShuaiServer.executor.submit(new ShuaiTask(request));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
