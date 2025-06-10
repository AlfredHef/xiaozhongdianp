-- 创建团购套餐表(所有ID使用BIGINT)
CREATE TABLE group_buying_package (
    id BIGINT AUTO_INCREMENT COMMENT '团购套餐 ID（主键，自增）',
    shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）',
    title VARCHAR(255) NOT NULL COMMENT '套餐标题，如"精选烧烤2-3人餐"',
    price DECIMAL(10, 2) NOT NULL COMMENT '套餐价格',
    description TEXT COMMENT '套餐描述',
    sales INT DEFAULT 0 COMMENT '套餐销量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    INDEX shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购套餐信息表';

-- 创建菜品表(所有ID使用BIGINT)
CREATE TABLE dish (
    id BIGINT AUTO_INCREMENT COMMENT '菜品 ID（主键，自增）',
    shop_id BIGINT NOT NULL COMMENT '所属商家 ID（关联 shop 表）',
    name VARCHAR(255) NOT NULL COMMENT '菜品名称',
    price DECIMAL(10, 2) COMMENT '菜品单价',
    description TEXT COMMENT '菜品描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    INDEX shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品信息表';

-- 创建套餐菜品关联表(所有ID使用BIGINT)
CREATE TABLE package_dish_relation (
    id BIGINT AUTO_INCREMENT COMMENT '关联记录 ID（主键，自增）',
    package_id BIGINT NOT NULL COMMENT '团购套餐 ID（关联 group_buying_package 表）',
    dish_id BIGINT NOT NULL COMMENT '菜品 ID（关联 dish 表）',
    quantity INT NOT NULL COMMENT '菜品数量',
    PRIMARY KEY (id),
    INDEX package_id (package_id),
    INDEX dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购套餐与菜品关联表';

-- 创建订单表(所有ID使用BIGINT)
CREATE TABLE group_buy_order (
    id BIGINT AUTO_INCREMENT COMMENT '订单ID（主键，自增）',
    user_id BIGINT NOT NULL COMMENT '用户ID（关联user表）',
    package_id BIGINT NOT NULL COMMENT '套餐ID（关联group_buying_package表）',
    shop_id BIGINT NOT NULL COMMENT '商家ID（关联shop表）',
    order_price DECIMAL(10, 2) NOT NULL COMMENT '订单实付金额',
    status TINYINT DEFAULT 1 COMMENT '订单状态：0-已取消，1-已购买，2-已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (id),
    INDEX user_id (user_id),
    INDEX package_id (package_id),
    INDEX shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购订单表';

-- 创建券码表(所有ID使用BIGINT)
CREATE TABLE voucher_code (
    id BIGINT AUTO_INCREMENT COMMENT '券码ID（主键，自增）',
    order_id BIGINT NOT NULL COMMENT '订单ID（关联group_buy_order表）',
    code VARCHAR(16) NOT NULL COMMENT '16位数字券码',
    qr_code_url TEXT COMMENT '二维码图片URL',
    status TINYINT DEFAULT 0 COMMENT '使用状态：0-未使用，1-已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    used_at DATETIME COMMENT '使用时间',
    PRIMARY KEY (id),
    UNIQUE KEY (code),
    INDEX order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='券码表';

-- 添加外键约束
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