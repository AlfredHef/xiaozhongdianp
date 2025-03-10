package com.fudan.xiaozhong_dianping.customer.service.impl;
import com.fudan.xiaozhong_dianping.customer.service.CaptchaService; // 引入验证码服务接口
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CaptchaService captchaService; // 注入验证码服务

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用户名规则：只能包含字母、数字和下划线，长度为3-20位
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    // 密码强度规则：至少包含一个大写字母、一个小写字母、一个数字，长度为8-20位
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");

    @Override
    public boolean register(User user, String captcha) {
        // 验证用户名是否符合规则
        if (!isValidUsername(user.getUsername())) {
            return false;
        }
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return false; // 用户名已存在
        }
        // 验证密码强度
        if (!isValidPassword(user.getPassword())) {
            return false;
        }
//        // 校验验证码（这里简单假设验证码为固定值，实际应用中需要根据具体情况实现）
//        if (!"1234".equals(captcha)) {
//            return false;

        // 校验验证码（替换原来的固定值判断）
        if (!captchaService.validateCaptcha(captcha)) { // 调用验证码服务的验证方法
            return false;
        }

        // 对密码进行加密处理
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        // 插入新用户
        return userMapper.insertUser(user) > 0;
    }

    @Override
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 验证用户名是否符合规则
     * @param username 用户名
     * @return 是否符合规则
     */
    private boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码强度
     * @param password 密码
     * @return 密码强度是否符合要求
     */
    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}