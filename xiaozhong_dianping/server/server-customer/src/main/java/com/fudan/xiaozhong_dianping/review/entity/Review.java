package com.fudan.xiaozhong_dianping.review.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name="review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 用户ID

    @Column(nullable = false)
    private Long merchantId; // 商户ID

    @Column(nullable = false, length = 1000)
    private String content; // 点评内容（≥15字）

    private Long parentId; // 父级ID（顶级点评为null）

    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间

    @Transient // 非数据库字段，用于存储子回复
    private List<Review> replies;


}
