package com.fudan.xiaozhong_dianping.customer.service;

import com.fudan.xiaozhong_dianping.customer.entity.User;
import com.fudan.xiaozhong_dianping.customer.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     */
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return false; // 用户名已存在
        }
        // 插入新用户
        return userMapper.insert(user) > 0;
    }

    /**
     * 用户登录
     */
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
}
