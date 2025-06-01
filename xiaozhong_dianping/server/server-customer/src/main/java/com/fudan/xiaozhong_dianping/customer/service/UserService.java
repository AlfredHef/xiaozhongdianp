package com.fudan.xiaozhong_dianping.customer.service;

import com.fudan.entity.RegisterRequest;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;

public interface UserService {
    /**
     * 用户注册
     * @param registerRequest 注册请求对象，包含用户信息和验证码
     * @return 注册是否成功
     */
    public RegisterResult register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     */
    User login(String username, String password);

    /**
     * 根据用户ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);
}
