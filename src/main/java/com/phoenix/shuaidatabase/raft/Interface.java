package com.phoenix.shuaidatabase.raft;

import java.util.Scanner;

public class Interface {
    public static void main(String[] args) {
        System.out.println("请输入将要运行的节点id：");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        ServerMain.main(new String[]{input});
    }
}
