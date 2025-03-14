package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.dto.ResponseDTO;
import com.fudan.entity.RegisterRequest;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
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
    public ResponseDTO<String> register(@RequestBody RegisterRequest registerRequest) {
        // 调用 service 处理注册
        RegisterResult result = userService.register(registerRequest);

        switch (result) {
            case SUCCESS:
                return new ResponseDTO<>(200, "注册成功", null);
            case USERNAME_INVALID:
                return new ResponseDTO<>(400, "注册失败，用户名格式不符合要求", null);
            case USERNAME_EXISTS:
                return new ResponseDTO<>(400, "注册失败，用户名已存在", null);
            case PASSWORD_INVALID:
                return new ResponseDTO<>(400, "注册失败，密码强度不足", null);
            case CAPTCHA_INVALID:
                return new ResponseDTO<>(400, "注册失败，验证码错误", null);
            default:
                return new ResponseDTO<>(400, "注册失败，未知错误", null);
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
