package com.fudan.xiaozhong_dianping.customer.service.impl;

import com.fudan.entity.RegisterRequest;
import com.fudan.xiaozhong_dianping.customer.service.CaptchaService; // 引入验证码服务接口
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用户名规则：只能包含字母、数字和下划线，长度为3-20位
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // 密码强度规则：密码必须包含数字和字母，长度为6-20位
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,20}$");

    @Override
    public RegisterResult register(RegisterRequest registerRequest) {
        // 从 RegisterRequest 中获取用户数据和验证码信息
        User user = registerRequest.getUser();
        String captchaId = registerRequest.getCaptchaId();
        String captchaText = registerRequest.getCaptchaText();

        // 验证用户名是否符合规则
        if (!isValidUsername(user.getUsername())) {
            return RegisterResult.USERNAME_INVALID;
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return RegisterResult.USERNAME_EXISTS;
        }

        // 验证密码强度
        if (!isValidPassword(user.getPassword())) {
            return RegisterResult.PASSWORD_INVALID;
        }

        // 此处移除了验证码的验证
        // 只需前端通过 verify 接口验证即可，后端不需要再验证

        // 对密码进行加密处理
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        // 插入新用户
        if (userMapper.insertUser(user) > 0) {
            return RegisterResult.SUCCESS;
        }

        return RegisterResult.USERNAME_EXISTS; // 插入失败，可能是用户名已存在
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user; // 返回用户对象
        }
        return null; // 验证失败返回null
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.findById(id);
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

