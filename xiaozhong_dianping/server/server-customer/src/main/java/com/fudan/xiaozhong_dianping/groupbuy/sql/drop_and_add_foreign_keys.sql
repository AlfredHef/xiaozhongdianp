-- 更安全的外键约束添加脚本
-- 使用存储过程处理外键的删除和添加

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 创建存储过程来安全地删除外键（如果存在）然后添加
DELIMITER //
CREATE PROCEDURE safe_add_foreign_key(
    IN p_table VARCHAR(255),
    IN p_constraint VARCHAR(255),
    IN p_definition VARCHAR(255)
)
BEGIN
    -- 检查外键是否存在
    SELECT COUNT(1) INTO @exists
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND CONSTRAINT_NAME = p_constraint;
      
    -- 如果存在则删除
    IF @exists > 0 THEN
        SET @drop_sql = CONCAT('ALTER TABLE ', p_table, ' DROP FOREIGN KEY ', p_constraint);
        PREPARE stmt FROM @drop_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    -- 添加新的外键约束
    SET @add_sql = CONCAT('ALTER TABLE ', p_table, ' ADD CONSTRAINT ', p_constraint, ' ', p_definition);
    PREPARE stmt FROM @add_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- 调用存储过程添加各个外键
CALL safe_add_foreign_key('group_buying_package', 'fk_package_shop', 'FOREIGN KEY (shop_id) REFERENCES shop(id)');
CALL safe_add_foreign_key('dish', 'fk_dish_shop', 'FOREIGN KEY (shop_id) REFERENCES shop(id)');
CALL safe_add_foreign_key('group_buy_order', 'fk_order_shop', 'FOREIGN KEY (shop_id) REFERENCES shop(id)');
CALL safe_add_foreign_key('group_buy_order', 'fk_order_user', 'FOREIGN KEY (user_id) REFERENCES user(id)');
CALL safe_add_foreign_key('package_dish_relation', 'fk_relation_package', 'FOREIGN KEY (package_id) REFERENCES group_buying_package(id)');
CALL safe_add_foreign_key('group_buy_order', 'fk_order_package', 'FOREIGN KEY (package_id) REFERENCES group_buying_package(id)');
CALL safe_add_foreign_key('package_dish_relation', 'fk_relation_dish', 'FOREIGN KEY (dish_id) REFERENCES dish(id)');
CALL safe_add_foreign_key('voucher_code', 'fk_voucher_order', 'FOREIGN KEY (order_id) REFERENCES group_buy_order(id) ON DELETE CASCADE');

-- 删除存储过程
DROP PROCEDURE IF EXISTS safe_add_foreign_key;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1; 