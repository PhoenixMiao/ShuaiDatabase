package com.phoenix.shuaidatabase.single;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Test {

    public static void main(String[] args) throws Exception {

        ConcurrentLinkedQueue<ShuaiDB> dbs = new ConcurrentLinkedQueue<>();

        new Thread(new Thread() {
            @Override
            public void run() {
                //线程2
                while (true) {
//                    ConcurrentLinkedDeque<ShuaiDB> dbs1 = ShuaiServer.dbs;
//                    if (dbs1.get(0).getDict().containsKey(new ShuaiString("xxx"))) {
//                        break;
//                    }
                }
//                System.out.println("Jump out of the loop!");
            }
        }).start();

        new Thread(new Thread() {
            @Override
            public void run() {
                //线程1
//                ShuaiServer.dbs.get(0).getDict().put(new ShuaiString("xxx"),new ShuaiString("sss"));
            }
        }).start();

    }

}
