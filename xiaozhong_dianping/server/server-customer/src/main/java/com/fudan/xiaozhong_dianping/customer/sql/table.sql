-- 商家表（存储商家基本信息）
CREATE TABLE shop
(
    id             INT PRIMARY KEY AUTO_INCREMENT COMMENT '商家ID（主键，自增）',
    name           VARCHAR(255) NOT NULL COMMENT '商家名称（支持模糊搜索）',
    address        TEXT COMMENT '详细地址（支持地理信息检索）',
    business_hours VARCHAR(255) COMMENT '营业时间（格式：周一至周日 10:00-22:00）',
    phone          VARCHAR(20) COMMENT '联系电话',
    average_cost   DECIMAL(10, 2) COMMENT '人均消费金额（元）',
    rating         DECIMAL(2, 1) COMMENT '综合评分（范围0.0 - 5.0）',
    price_min      DECIMAL(10, 2) COMMENT '最低消费价格（元）',
    price_max      DECIMAL(10, 2) COMMENT '最高消费价格（元）',
    category_id    INT COMMENT '所属分类ID（关联category表）',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at     DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    FOREIGN KEY (category_id) REFERENCES category (id)
) COMMENT '商家信息主表，支持搜索、筛选、排序功能';

-- 分类表（管理商家分类信息）
CREATE TABLE category
(
    id   INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID（主键，自增）',
    name VARCHAR(255) NOT NULL UNIQUE COMMENT '分类名称（如"火锅"、"奶茶"，唯一约束）'
) COMMENT '商家分类字典表，用于搜索关键词匹配';

-- 商家图片表（存储商家相关图片）
CREATE TABLE shop_image
(
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID（主键，自增）',
    merchant_id INT  NOT NULL COMMENT '所属商家ID（关联merchant表）',
    image_url   TEXT NOT NULL COMMENT '图片存储URL（支持多图）',
    description TEXT COMMENT '图片描述（如"招牌菜品"、"门店外观"）',
    FOREIGN KEY (merchant_id) REFERENCES merchant (id)
) COMMENT '商家图片存储表，支持多图展示';

-- 搜索历史表（记录用户搜索行为）
CREATE TABLE search_history
(
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID（主键，自增）',
    user_id     bigint       NOT NULL COMMENT '用户ID（关联user表）',
    keyword     VARCHAR(255) NOT NULL COMMENT '搜索关键词',
    search_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    FOREIGN KEY (user_id) REFERENCES user (id)
) COMMENT '用户搜索历史记录表，用于快速搜索和行为分析';

-- 创建名为 user 的表，用于存储用户信息
CREATE TABLE user
(
    -- 定义 id 列，类型为 bigint，自动递增，作为表的主键
    -- 主键用于唯一标识表中的每一行记录
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- 定义 username 列，类型为 varchar(20)，表示可变长度字符串，最大长度为 20
    -- 该列不允许为空，因为每个用户都必须有一个用户名
    username   VARCHAR(20) NOT NULL,
    -- 定义 password 列，类型为 varchar(60)，最大长度为 60
    -- 该列不允许为空，因为每个用户都必须有一个密码
    password   VARCHAR(60) NOT NULL,
    -- 定义 created_at 列，类型为 timestamp
    -- 该列的默认值为当前时间戳，允许为空
    -- 用于记录用户创建的时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    -- 为 username 列添加唯一约束，确保表中每个用户名都是唯一的
    -- 防止出现重复的用户名
    CONSTRAINT username UNIQUE (username)
);