package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.dto.ResponseDTO;
import com.fudan.entity.RegisterRequest;

import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import com.fudan.xiaozhong_dianping.customer.properties.JwtProperties;
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseDTO<String> register(@RequestBody RegisterRequest registerRequest) {
        log.info("用户注册，请求为: {}", registerRequest);
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
     * 用户登录接口（修正后）
     */
    @PostMapping("/login")
    public ResponseDTO<String> login(@RequestBody User user) {
        try {
            User authenticatedUser = userService.login(user.getUsername(), user.getPassword());
            if (authenticatedUser != null) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", authenticatedUser.getId());
                String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
                // 存储到Redis，添加前缀
                String redisKey = "token:" + token;
                redisTemplate.opsForValue().set(redisKey, authenticatedUser.getId().toString(),
                        jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);
                return new ResponseDTO<>(200, "登录成功", "Bearer " + token);
            } else {
                return new ResponseDTO<>(401, "登录失败", null);
            }
        } catch (Exception e) {
            // 记录日志
            return new ResponseDTO<>(500, "服务器错误", null);
        }
    }

    /**
     * 用户退出接口（修正后）
     */
    @PostMapping("/logout")
    public ResponseDTO<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseDTO<>(400, "无效Token", null);
        }
        String token = authHeader.substring(7);
        // 使用与登录时一致的Redis键
        String redisKey = "token:" + token;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (exists != null && exists) {
            redisTemplate.delete(redisKey);
            return new ResponseDTO<>(200, "退出成功", null);
        } else {
            return new ResponseDTO<>(400, "无效Token", null);
        }
    }
}
