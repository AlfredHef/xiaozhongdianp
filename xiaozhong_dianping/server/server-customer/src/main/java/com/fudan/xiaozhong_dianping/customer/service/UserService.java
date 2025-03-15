package com.fudan.xiaozhong_dianping.customer.service;


import com.fudan.dto.UserLoginDTO;
import com.fudan.entity.RegisterRequest;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import com.fudan.entity.User;
/**
 * 用户服务接口
 * 定义了用户相关操作的标准接口，如用户注册和登录
 */
public interface UserService {
    /**
     * 用户注册
     * @param registerRequest 注册请求对象，包含用户信息和验证码
     * @return 注册是否成功
     */
    public RegisterResult register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * @param userLoginDTO 用户登录数据传输对象，包含用户登录所需的信息
     * @return 登录成功的用户对象，如果登录失败则返回null
     */
    User login(UserLoginDTO userLoginDTO);
}
