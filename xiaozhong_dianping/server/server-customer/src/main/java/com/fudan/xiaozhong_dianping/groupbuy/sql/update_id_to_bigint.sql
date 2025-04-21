-- 将所有ID类型更改为BIGINT的脚本

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 修改团购套餐表 group_buying_package
ALTER TABLE group_buying_package 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '团购套餐 ID（主键，自增）',
    MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改菜品表 dish
ALTER TABLE dish 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '菜品 ID（主键，自增）',
    MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）';

-- 修改套餐菜品关联表 package_dish_relation
ALTER TABLE package_dish_relation 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '关联记录 ID（主键，自增）',
    MODIFY COLUMN package_id BIGINT NOT NULL COMMENT '团购套餐 ID（关联 group_buying_package 表）',
    MODIFY COLUMN dish_id BIGINT NOT NULL COMMENT '菜品 ID（关联 dish 表）';

-- 修改订单表 group_buy_order (id和user_id已经是BIGINT，只需修改package_id和shop_id)
ALTER TABLE group_buy_order 
    MODIFY COLUMN package_id BIGINT NOT NULL COMMENT '套餐ID（关联group_buying_package表）',
    MODIFY COLUMN shop_id BIGINT NOT NULL COMMENT '商家ID（关联shop表）';

-- voucher_code表的id和order_id已经是BIGINT，不需要修改

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 类型修改后，现在可以添加外键约束
-- 添加shop_id外键约束
ALTER TABLE group_buying_package ADD CONSTRAINT fk_package_shop FOREIGN KEY (shop_id) REFERENCES shop(id);
ALTER TABLE dish ADD CONSTRAINT fk_dish_shop FOREIGN KEY (shop_id) REFERENCES shop(id);
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES shop(id);

-- 添加user_id外键约束
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user(id);

-- 添加package_id外键约束
ALTER TABLE package_dish_relation ADD CONSTRAINT fk_relation_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);
ALTER TABLE group_buy_order ADD CONSTRAINT fk_order_package FOREIGN KEY (package_id) REFERENCES group_buying_package(id);

-- 添加dish_id外键约束
ALTER TABLE package_dish_relation ADD CONSTRAINT fk_relation_dish FOREIGN KEY (dish_id) REFERENCES dish(id);

-- 添加order_id外键约束
ALTER TABLE voucher_code ADD CONSTRAINT fk_voucher_order FOREIGN KEY (order_id) REFERENCES group_buy_order(id) ON DELETE CASCADE; 