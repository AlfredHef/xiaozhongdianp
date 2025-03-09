package com.fudan.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password; // 存储加密后的密码
    private LocalDateTime createTime;
}