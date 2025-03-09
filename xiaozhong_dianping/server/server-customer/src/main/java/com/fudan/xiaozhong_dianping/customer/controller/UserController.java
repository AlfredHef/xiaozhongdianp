package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.xiaozhong_dianping.customer.entity.User;
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseDto<String> register(@RequestBody User user) {
        boolean isRegistered = userService.register(user);
        if (isRegistered) {
            return new ResponseDto<>(200, "注册成功", null);
        } else {
            return new ResponseDto<>(400, "注册失败，用户名可能已存在", null);
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ResponseDto<String> login(@RequestBody User user) {
        boolean isLoginSuccessful = userService.login(user.getUsername(), user.getPassword());
        if (isLoginSuccessful) {
            return new ResponseDto<>(200, "登录成功", null);
        } else {
            return new ResponseDto<>(401, "登录失败，用户名或密码错误", null);
        }
    }
}
