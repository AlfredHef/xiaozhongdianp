-- 添加外键约束脚本

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 添加shop_id外键约束
-- 先尝试删除可能存在的外键约束，如果不存在会报错，但不影响后续操作
ALTER TABLE group_buying_package 
ADD CONSTRAINT fk_package_shop FOREIGN KEY (shop_id) REFERENCES shop(id);

ALTER TABLE dish 
ADD CONSTRAINT fk_dish_shop FOREIGN KEY (shop_id) REFERENCES shop(id);

ALTER TABLE group_buy_order 
ADD CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES shop(id);

-- 添加user_id外键约束
ALTER TABLE group_buy_order 
ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user(id);

-- 添加package_id外键约束
ALTER TABLE package_dish_relation 
ADD CONSTRAINT fk_relation_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);

ALTER TABLE group_buy_order 
ADD CONSTRAINT fk_order_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);

-- 添加dish_id外键约束
ALTER TABLE package_dish_relation 
ADD CONSTRAINT fk_relation_dish FOREIGN KEY (dish_id) REFERENCES dish(id);

-- 添加order_id外键约束
ALTER TABLE voucher_code 
ADD CONSTRAINT fk_voucher_order FOREIGN KEY (order_id) REFERENCES group_buy_order(id) ON DELETE CASCADE;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1; 