package com.phoenix.shuaidatabase.raft.service;

import com.phoenix.shuaidatabase.single.ShuaiReply;
import com.phoenix.shuaidatabase.single.ShuaiRequest;

import java.io.IOException;

public interface ShuaiService {

    ShuaiReply set(ShuaiRequest request) throws IOException;

    ShuaiReply get(ShuaiRequest request);
}
