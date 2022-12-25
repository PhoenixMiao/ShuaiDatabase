package com.phoenix.shuaidatabase.single;

import java.util.Objects;

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

    @Override
    public String toString() {
        return key.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShuaiExpireKey expireKey = (ShuaiExpireKey) o;
        return Objects.equals(key, expireKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
