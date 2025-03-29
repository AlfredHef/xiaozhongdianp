-- 为商家表添加示例图片数据
-- 假设商家ID从1开始，请根据实际情况调整

-- 为ID为1的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (1, 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg', '门店外观');

-- 为ID为2的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (2, 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg', '招牌菜品');

-- 为ID为3的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (3, 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png', '店内环境');

-- 为ID为4的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (4, 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg', '特色美食');

-- 为ID为5的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (5, 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg', '门店外观');

-- 为ID为6的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (6, 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg', '招牌菜品');

-- 为ID为7的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (7, 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg', '店内环境');

-- 为ID为8的商家插入图片
INSERT INTO shop_image (shop_id, image_url, description) 
VALUES (8, 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg', '特色美食');

-- 注意：执行此脚本前，请确保shop表中已有对应ID的商家数据
-- 如果商家ID不同，请修改上述SQL语句中的shop_id值 