package com.fudan.xiaozhong_dianping.customer.controller;

import com.fudan.dto.ResponseDTO;
import com.fudan.dto.UserLoginDTO;
import com.fudan.entity.RegisterRequest;

import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import com.fudan.xiaozhong_dianping.customer.properties.JwtProperties;
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
     *
     * @param registerRequest 注册请求对象，包含用户注册所需的信息
     * @return 返回注册结果的DTO对象
     */
    @PostMapping("/register")
    public ResponseDTO<String> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Received register request: {}", registerRequest);
        // 调用 service 处理注册
        RegisterResult result = userService.register(registerRequest);

        // 根据注册结果返回不同的响应
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
     *
     * @param userLoginDTO 用户对象，包含用户名和密码以及验证码文本
     * @return 返回登录结果的DTO对象，包括token
     */
    @PostMapping("/login")
    public ResponseDTO<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 校验验证码（假设验证码存储在Redis中，键为 "captcha:用户标识"）
        String storedCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + userLoginDTO.getCaptchaId());
        if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(userLoginDTO.getCaptchaText())) {
            if(storedCaptcha == null){
                System.out.println("验证码已过期");
            }
            return new ResponseDTO<>(400, "验证码错误", null);
        }
        try {
            User authenticatedUser = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            if (authenticatedUser != null) {
                // 生成JWT token
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
            // 详细记录异常
            e.printStackTrace(); // 或使用日志框架
            System.out.println("登录异常: " + e.getMessage());
            return new ResponseDTO<>(500, "服务器错误", null);
        }
    }

    /**
     * 用户登出接口
     *
     * @param authHeader 授权头部，包含Bearer token
     * @return 返回登出结果的DTO对象
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
        // 如果token存在，则删除之，完成登出
        if (exists != null && exists) {
            redisTemplate.delete(redisKey);
            return new ResponseDTO<>(200, "退出成功", null);
        } else {
            return new ResponseDTO<>(400, "无效Token", null);
        }
    }

    /**
     * 根据用户ID获取用户信息（不包含密码）
     *
     * @param userId 用户ID
     * @return 返回用户信息的DTO对象
     */
    @GetMapping("/{userId}")
    public ResponseDTO<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                // 创建一个新的User对象，不包含密码信息
                User safeUser = new User();
                safeUser.setId(user.getId());
                safeUser.setUsername(user.getUsername());
                safeUser.setCreatedAt(user.getCreatedAt());
                return new ResponseDTO<>(200, "获取用户信息成功", safeUser);
            } else {
                return new ResponseDTO<>(404, "用户不存在", null);
            }
        } catch (Exception e) {
            log.error("获取用户信息异常: " + e.getMessage(), e);
            return new ResponseDTO<>(500, "服务器错误", null);
        }
    }
}
