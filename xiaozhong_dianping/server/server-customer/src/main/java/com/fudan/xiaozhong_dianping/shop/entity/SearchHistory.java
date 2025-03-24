package com.fudan.xiaozhong_dianping.shop.entity;
import com.fudan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * SearchHistory 类用于表示用户的搜索历史记录，
 * 该记录包含了搜索历史的唯一标识、关联的用户、搜索关键词以及搜索时间，
 * 可用于快速搜索功能和用户行为分析。
 * 借助 Lombok 注解，自动生成了 getter、setter、构造函数等常用方法，简化了代码编写。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    private Long id;
    private Long userId;
    private String keyword;
    private Date searchTime;
}