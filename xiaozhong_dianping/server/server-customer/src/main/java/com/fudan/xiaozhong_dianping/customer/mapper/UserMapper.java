package com.fudan.xiaozhong_dianping.customer.mapper;

import com.fudan.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    /**
     * 通过用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT id, username, password, created_at AS createdAt FROM user WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 通过用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("SELECT id, username, password, created_at AS createdAt FROM user WHERE id = #{id}")
    User findById(Long id);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 用户数量（0 表示可用，1 及以上表示已存在）
     */
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int checkUsernameExists(String username);

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO user (username, password, created_at) VALUES (#{username}, #{password}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);
}

