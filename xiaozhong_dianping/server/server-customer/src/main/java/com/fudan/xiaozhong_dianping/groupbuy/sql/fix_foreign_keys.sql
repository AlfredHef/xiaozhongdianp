-- 修复外键不兼容问题的脚本

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 方法1：修改group_buying_package表的shop_id列，使其与shop表的id列类型完全匹配
-- 假设shop.id是BIGINT类型，而group_buying_package.shop_id是INT
ALTER TABLE group_buying_package MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改dish表的shop_id列
ALTER TABLE dish MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改group_buy_order表的shop_id列
ALTER TABLE group_buy_order MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '商家ID（关联shop表）';

-- 方法2：修改group_buying_package表的shop_id列的其他属性（如有符号/无符号）
-- 假设shop.id是无符号INT，而group_buying_package.shop_id是有符号INT
-- ALTER TABLE group_buying_package MODIFY COLUMN shop_id INT UNSIGNED NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改dish表的shop_id列
-- ALTER TABLE dish MODIFY COLUMN shop_id INT UNSIGNED NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改group_buy_order表的shop_id列
-- ALTER TABLE group_buy_order MODIFY COLUMN shop_id INT UNSIGNED NOT NULL COMMENT '商家ID（关联shop表）';

-- 现在尝试添加外键
ALTER TABLE group_buying_package ADD CONSTRAINT fk_package_shop FOREIGN KEY (shop_id) REFERENCES shop(id);
ALTER TABLE dish ADD CONSTRAINT fk_dish_shop FOREIGN KEY (shop_id) REFERENCES shop(id);
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES shop(id);

-- 其他外键也一样处理
-- package_id可能也需要类型兼容性修改
ALTER TABLE package_dish_relation MODIFY COLUMN package_id BIGINT NOT NULL COMMENT '团购套餐 ID（关联 group_buying_package 表）';
ALTER TABLE group_buy_order MODIFY COLUMN package_id BIGINT NOT NULL COMMENT '套餐ID（关联group_buying_package表）';

-- dish_id可能也需要类型兼容性修改
ALTER TABLE package_dish_relation MODIFY COLUMN dish_id BIGINT NOT NULL COMMENT '菜品 ID（关联 dish 表）';

-- 添加其余外键
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user(id);
ALTER TABLE package_dish_relation ADD CONSTRAINT fk_relation_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);
ALTER TABLE package_dish_relation ADD CONSTRAINT fk_relation_dish FOREIGN KEY (dish_id) REFERENCES dish(id);
ALTER TABLE voucher_code ADD CONSTRAINT fk_voucher_order FOREIGN KEY (order_id) REFERENCES group_buy_order(id) ON DELETE CASCADE;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1; 