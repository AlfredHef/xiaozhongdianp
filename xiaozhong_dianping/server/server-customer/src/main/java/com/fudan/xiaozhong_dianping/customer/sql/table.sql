-- 用户表（存储用户基本信息）
CREATE TABLE user (
                      id bigint auto_increment primary key,
                      username varchar(20) not null,
                      password varchar(60) not null,
                      created_at timestamp default CURRENT_TIMESTAMP null,
                      constraint username unique (username)
) COMMENT '用户信息表，存储用户的基本信息';
-- 创建商家分类字典表
CREATE TABLE category (
                          id INT AUTO_INCREMENT COMMENT '分类 ID（主键，自增）',
                          name VARCHAR(255) NOT NULL COMMENT '分类名称（如"火锅"、"奶茶"，唯一约束）',
                          PRIMARY KEY (id),
                          UNIQUE KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家分类字典表，用于搜索关键词匹配';

-- 创建用户搜索历史记录表
CREATE TABLE search_history (
                                id INT AUTO_INCREMENT COMMENT '历史记录 ID（主键，自增）',
                                user_id BIGINT NOT NULL COMMENT '用户 ID（关联 user 表）',
                                keyword VARCHAR(255) NOT NULL COMMENT '搜索关键词',
                                search_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
                                PRIMARY KEY (id),
                                FOREIGN KEY (user_id) REFERENCES user(id),
                                INDEX user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户搜索历史记录表，用于快速搜索和行为分析';

-- 创建商家信息主表
CREATE TABLE shop (
                      id INT AUTO_INCREMENT COMMENT '商家 ID（主键，自增）',
                      name VARCHAR(255) NOT NULL COMMENT '商家名称（支持模糊搜索）',
                      address TEXT COMMENT '详细地址（支持地理信息检索）',
                      business_hours VARCHAR(255) COMMENT '营业时间（格式：周一至周日 10:00-22:00）',
                      phone VARCHAR(20) COMMENT '联系电话',
                      average_cost DECIMAL(10, 2) COMMENT '人均消费金额（元）',
                      rating DECIMAL(2, 1) COMMENT '综合评分（范围 0.0 - 5.0）',
                      price_min DECIMAL(10, 2) COMMENT '最低消费价格（元）',
                      price_max DECIMAL(10, 2) COMMENT '最高消费价格（元）',
                      category_id INT COMMENT '所属分类 ID（关联 category 表）',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                      updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                      PRIMARY KEY (id),
                      FOREIGN KEY (category_id) REFERENCES category(id),
                      INDEX category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家信息主表，支持搜索、筛选、排序功能';

-- 创建商家图片存储表
CREATE TABLE shop_image (
                            id INT AUTO_INCREMENT COMMENT '图片 ID（主键，自增）',
                            shop_id INT NOT NULL COMMENT '所属商家 ID（关联 shop 表）',
                            image_url TEXT NOT NULL COMMENT '图片存储 URL（支持多图）',
                            description TEXT COMMENT '图片描述（如"招牌菜品"、"门店外观"）',
                            PRIMARY KEY (id),
                            FOREIGN KEY (shop_id) REFERENCES shop(id),
                            INDEX shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家图片存储表，支持多图展示';

