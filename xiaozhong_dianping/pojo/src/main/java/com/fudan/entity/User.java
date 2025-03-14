package com.fudan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;  // Java 字段名为 'createdAt'，与数据库中的 'created_at' 映射
}
