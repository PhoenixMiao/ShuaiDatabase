package com.phoenix.shuaidatabase.single;

import groovy.json.internal.Byt;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TestUnit {

    private SocketChannel client;

    private ByteBuffer buffer1;

    private ByteBuffer buffer2;

    public TestUnit(SocketChannel client, ByteBuffer buffer1, ByteBuffer buffer2) {
        this.client = client;
        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
    }

    public SocketChannel getClient() {
        return client;
    }

    public void setClient(SocketChannel client) {
        this.client = client;
    }

    public ByteBuffer getBuffer1() {
        return buffer1;
    }

    public void setBuffer1(ByteBuffer buffer1) {
        this.buffer1 = buffer1;
    }

    public ByteBuffer getBuffer2() {
        return buffer2;
    }

    public void setBuffer2(ByteBuffer buffer2) {
        this.buffer2 = buffer2;
    }
}
