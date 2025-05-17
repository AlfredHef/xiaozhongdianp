-- 邀请码表
CREATE TABLE `invitation_code` (
                                   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
                                   `code` VARCHAR(8) NOT NULL COMMENT '邀请码',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_user_id` (`user_id`),
                                   UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '用户邀请码表';

-- 邀请记录表
CREATE TABLE `invitation_record` (
                                     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `inviter_id` BIGINT UNSIGNED NOT NULL COMMENT '邀请人ID',
                                     `invitee_id` BIGINT UNSIGNED NOT NULL COMMENT '被邀请人ID',
                                     `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
                                     `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单实付金额',
                                     `order_time` DATETIME NOT NULL COMMENT '订单时间',
                                     `is_valid` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效邀请',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_inviter_id` (`inviter_id`),
                                     KEY `idx_invitee_id` (`invitee_id`),
                                     UNIQUE KEY `uk_invitee_id` (`invitee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '邀请记录表';

-- 邀请奖励表
CREATE TABLE `invitation_reward` (
                                     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
                                     `coupon_id` BIGINT UNSIGNED NOT NULL COMMENT '优惠券ID',
                                     `invitation_count` INT NOT NULL COMMENT '触发奖励的邀请数量',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '邀请奖励表';