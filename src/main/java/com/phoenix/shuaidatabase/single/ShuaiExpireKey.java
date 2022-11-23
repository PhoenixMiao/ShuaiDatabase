package com.phoenix.shuaidatabase.single;

public class ShuaiExpireKey extends ShuaiObject{

    private ShuaiString key;

    public ShuaiExpireKey(ShuaiString key) {
        this.key = key;
    }

    public ShuaiString getKey() {
        return key;
    }

    public void setKey(ShuaiString key) {
        this.key = key;
    }
}
