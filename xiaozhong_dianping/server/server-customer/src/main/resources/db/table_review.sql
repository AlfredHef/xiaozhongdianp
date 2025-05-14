CREATE TABLE `review` (
                          `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（关联user表）',
                          `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID（关联shop表）',
                          `content` VARCHAR(1000) NOT NULL COMMENT '点评内容（≥15字）',
                          `parent_id` BIGINT UNSIGNED COMMENT '父级点评ID（顶级点评为NULL）',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          INDEX `idx_merchant` (`merchant_id`) COMMENT '按商户查询索引',
                          INDEX `idx_user` (`user_id`) COMMENT '按用户查询索引',
                          INDEX `idx_parent` (`parent_id`) COMMENT '按父级ID查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '点评/回复表';