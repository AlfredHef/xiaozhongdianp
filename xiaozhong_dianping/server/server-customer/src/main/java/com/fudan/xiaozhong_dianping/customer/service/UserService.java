package com.fudan.xiaozhong_dianping.customer.service;

import com.fudan.entity.User;

public interface UserService {
    /**
     * 用户注册
     * @param user            用户信息
     * @param captchaId       验证码ID
     * @param userInputCaptcha 用户输入的验证码
     * @return 注册是否成功
     */
    boolean register(User user, String captchaId, String userInputCaptcha);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     */
    boolean login(String username, String password);
}