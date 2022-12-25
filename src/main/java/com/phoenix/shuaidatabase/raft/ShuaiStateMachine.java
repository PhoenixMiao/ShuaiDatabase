package com.phoenix.shuaidatabase.raft;

import com.phoenix.shuaidatabase.single.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ShuaiStateMachine implements StateMachine{

    static {
        ShuaiServer.open();
    }

    public ShuaiStateMachine() {}

    @Override
    public void writeSnapshot(String snapshotDir) {
        try{
            new ShuaiTask.RdbProduce(false,true).call();
            FileUtils.copyDirectory(new File(ShuaiConstants.PERSISTENCE_PATH),new File(snapshotDir));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readSnapshot(String snapshotDir) {
        try{
            FileUtils.copyDirectory(new File(snapshotDir),new File(ShuaiConstants.PERSISTENCE_PATH));
            ShuaiServer.dbs = new ConcurrentLinkedDeque<ShuaiDB>(){{
                add(new ShuaiDB());
                add(new ShuaiDB());
                add(new ShuaiDB());
            }};
            ShuaiServer.loadRdbFile();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void apply(byte[] dataBytes) {
        try{
            ShuaiRequest request = (ShuaiRequest) ShuaiTalk.backToObject(dataBytes);
            ShuaiServer.executor.submit(new ShuaiTask(request));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
