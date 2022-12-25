package com.phoenix.shuaidatabase.single;

import com.google.common.base.Objects;

public class ShuaiBuffer extends ShuaiObject{

    private String value;

    public ShuaiBuffer(String value) {
        this.value = value;
    }

    public int length() {
        return value.length();
    }

    public String substring(int begin,int end) {
        String oldValue = value;
        return oldValue.substring(begin,end);
    }

    public String substring(int begin) {
        String oldValue = value;
        return oldValue.substring(begin);
    }

    public void append(String str) {
        synchronized (this) {
            value += str;
        }
    }

    public void setRange(Integer offset, String str) {
        StringBuilder builder = new StringBuilder(value);
        builder.setLength(offset);
        builder.append(str);
        synchronized (this) {
            value = builder.toString();
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShuaiBuffer that = (ShuaiBuffer) o;
        return java.util.Objects.equals(value, that.value);
    }
}
