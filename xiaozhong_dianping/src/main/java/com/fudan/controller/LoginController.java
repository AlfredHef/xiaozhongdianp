package com.fudan;

import java.util.Scanner;

public class LoginController {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入用户名: ");
        String inputUsername = scanner.nextLine();

        System.out.print("请输入密码: ");
        String inputPassword = scanner.nextLine();

        if (USERNAME.equals(inputUsername) && PASSWORD.equals(inputPassword)) {
            System.out.println("登录成功，跳转到首页...");
            // 这里可以调用主页的类，如 HomePage.show();
        } else {
            System.out.println("用户名或密码错误，请重试！");
        }

        scanner.close();
    }
}
