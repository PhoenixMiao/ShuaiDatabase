package com.phoenix.shuaidatabase.single;

import java.util.concurrent.ConcurrentHashMap;

public class ShuaiHash extends ShuaiObject {

    private ConcurrentHashMap<ShuaiString, ShuaiString> hashMap;

    public ShuaiHash() {
        this.hashMap = new ConcurrentHashMap<>();
        this.objectType = ShuaiObjectType.SHUAI_HASH;
    }

    public ConcurrentHashMap<ShuaiString, ShuaiString> getHashMap() {
        return hashMap;
    }

    public ShuaiReply hGet(String[] argv, ShuaiDB db) {
        ShuaiString res;
        String field = argv[2];
        if (!hashMap.containsKey(new ShuaiString(field))) res = new ShuaiString("null");
        else res = new ShuaiString(hashMap.get(new ShuaiString(field)).toString());
        return new ShuaiReply(ShuaiReplyStatus.OK, res);
    }

    public static ShuaiReply hSet(String[] argv, ShuaiDB db) {
        String key = argv[1];
        String field = argv[2];
        String value = argv[3];
        ShuaiHash shuaiHash;
        try {
            if (db.getDict().containsKey(new ShuaiString(argv[1]))) shuaiHash = (ShuaiHash) db.getDict().get(new ShuaiString(argv[1]));
            else {
                shuaiHash = new ShuaiHash();
                db.getDict().put(new ShuaiString(key), shuaiHash);
            }
            shuaiHash.hashMap.put(new ShuaiString(field), new ShuaiString(value));
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString("OK"));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }

    }

    public ShuaiReply hExist(String[] argv, ShuaiDB db) {
        ShuaiString res;
        ShuaiString field = new ShuaiString(argv[2]);
        if (hashMap.containsKey(field)) res = new ShuaiString("1");
        else res = new ShuaiString("0");
        return new ShuaiReply(ShuaiReplyStatus.OK, res);
    }

    public ShuaiReply hDel(String[] argv, ShuaiDB db) {
        int cnt = 0;
        String[] fields = argv[2].split(" ");
        for (String s : fields) {
            ShuaiString field = new ShuaiString(s);
            if (hashMap.containsKey(field)) {
                cnt++;
                hashMap.remove(field);
            }
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(cnt+""));
    }

    public ShuaiReply hLen(String[] argv, ShuaiDB db) {
        ShuaiString res = new ShuaiString(hashMap.size() + "");
        return new ShuaiReply(ShuaiReplyStatus.OK, res);
    }

    public ShuaiReply hGetAll(String[] argv, ShuaiDB db) {
        StringBuilder res = new StringBuilder();
        int i = 0;
        for (ShuaiString field : hashMap.keySet()) {
            ShuaiObject obj = hashMap.get(field);
            res.append(i * 2).append(") ").append(field.toString()).append('\n');
            res.append(i * 2 + 1).append(") ").append(obj.toString()).append('\n');
            i++;
        }
        if(i == 0) return new ShuaiReply(ShuaiReplyStatus.OK,new ShuaiString("(Empty Hash)"));
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res.toString()));
    }

    public static ShuaiReply hMSet(String[] argv, ShuaiDB db) {
        String key = argv[1];
        String field = argv[2];
        String value = argv[3];
        ShuaiHash shuaiHash;
        try {
            if (db.getDict().containsKey(new ShuaiString(argv[1])))
                shuaiHash = (ShuaiHash) db.getDict().get(new ShuaiString(argv[1]));
            else {
                shuaiHash = new ShuaiHash();
                db.getDict().put(new ShuaiString(key), shuaiHash);
            }
            String[] pairs = value.split(" ");
            if (pairs.length % 2 == 0) {
                return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
            }
            shuaiHash.hashMap.put(new ShuaiString(field), new ShuaiString(pairs[0]));
            for (int i = 1; i < pairs.length; i += 2) {
                shuaiHash.hashMap.put(new ShuaiString(pairs[i]), new ShuaiString(pairs[i + 1]));
            }
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString("OK"));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new ShuaiReply(ShuaiReplyStatus.INNER_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        int i = 0;
        for (ShuaiString field : hashMap.keySet()) {
            ShuaiObject obj = hashMap.get(field);
            res.append(i * 2).append(") ").append(field.toString()).append('\n');
            res.append(i * 2 + 1).append(") ").append(obj.toString()).append('\n');
            i++;
        }
        if(i == 0) return "(Empty Hash)";
        return res.toString();
    }
}
