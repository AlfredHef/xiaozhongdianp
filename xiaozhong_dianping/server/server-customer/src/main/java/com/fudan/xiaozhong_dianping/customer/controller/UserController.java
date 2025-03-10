package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.dto.ResponseDTO;
import com.fudan.entity.User;
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
    public ResponseDTO<String> register(@RequestBody User user, @RequestParam("captcha") String captcha) {
        boolean isRegistered = userService.register(user, captcha);
        if (isRegistered) {
            return new ResponseDTO<>(200, "注册成功", null);
        } else {
            return new ResponseDTO<>(400, "注册失败，可能是用户名已存在、格式不符合要求、密码强度不足或验证码错误", null);
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ResponseDTO<String> login(@RequestBody User user) {
        boolean isLoginSuccessful = userService.login(user.getUsername(), user.getPassword());
        if (isLoginSuccessful) {
            return new ResponseDTO<>(200, "登录成功", null);
        } else {
            return new ResponseDTO<>(401, "登录失败，用户名或密码错误", null);
        }
    }
}