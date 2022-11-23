package com.phoenix.shuaidatabase.single;

import java.io.*;

public class ShuaiTalk implements Serializable {

    private static final long serialVersionUID = -219988432063763446L;

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static ShuaiTalk backToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream  = new ObjectInputStream(byteArrayInputStream);
        return (ShuaiTalk) objectInputStream.readObject();
    }

}
