# 2025_se - 小众点评网站

## 项目描述
本项目是一个类似大众点评功能的“小众点评”网站。本学期我们将逐步实现网站的核心功能，Lab1 的重点是实现用户登录和注册功能。

## 功能目标
### Lab1: 用户登录和注册功能
1. **用户注册**：
   - 用户输入用户名、密码和验证码，创建账号。
   - 用户名要求：
     - 只允许包含字母、数字、下划线（`_`）。
     - 长度在 4～20 个字符之间。
     - 不得与其他用户重复。
   - 密码要求：
     - 长度不小于 6 位。
     - 必须包含数字和字母。
     - 在界面上显示密码强度提示（弱/中/强），标准自拟。
   - 验证码：
     - 用户注册时，页面显示一张带有随机数字的验证码图片。
     - 用户需要正确输入验证码。
     - 点击验证码图片可刷新验证码。

2. **用户登录**：
   - 用户输入用户名和密码，验证成功后跳转至首页。

3. **数据存储**：
   - 账号信息存入数据库。
   - 密码不得明文存储，需进行加密处理。

4. **前后端分离开发**：
   - 前端负责发送数据（如用户名、密码、验证码）。
   - 后端负责处理数据并返回结果（如注册成功、登录成功、错误提示等）。

5. **异常处理与错误提示**：
   - 对常见异常情况进行处理，例如：
     - 用户名已存在。
     - 密码错误。
     - 验证码错误。
   - 在界面上显示友好的错误提示信息。

## 文件结构
project/
│
├── src/ # 源代码目录
│ ├── api/ # API 接口
│ ├── auth/ # 用户认证模块
│ ├── config/ # 配置文件
│ ├── models/ # 数据库模型
│ ├── utils/ # 工具函数
│ └── main.py # 主程序入口
│
├── tests/ # 测试代码目录
│ ├── test_auth.py # 用户认证测试
│ └── test_utils.py # 工具函数测试
│
├── frontend/ # 前端代码目录
│ ├── public/ # 静态资源
│ └── src/ # 前端源代码
│
├── docs/ # 文档目录
│ └── README.md # 项目说明文档
│
├── requirements.txt # 项目依赖
└── .gitignore # Git 忽略文件

## 开发规范
1. **Commit Message 规范**：
   - `feat`: 新功能
   - `fix`: 修复 bug
   - `docs`: 文档更新
   - `style`: 代码格式调整
   - `refactor`: 代码重构
   - `test`: 测试相关
   - `chore`: 构建过程或辅助工具的变动

2. **分支管理**：
   - 从 `develop` 分支创建新分支进行开发，命名格式为 `feature/功能名称`。
   - 开发完成后，提交 Merge Request 到 `develop` 分支。

3. **代码审查**：
   - 每位同学的代码需经过至少一位其他同学的审查后才能合并。

## 安装与运行
1. 克隆仓库：
   ```bash
   git clone git@codehub.devcloud.cn-east-3.huaweicloud.com:256c0d48d98941eb893126abc5d5bb5e/2025_se.git
- 可参考文件组织：
![img.png](img.png)

- mapper层可以替代dao文件夹。以下是具体说明：
- 功能等价性：二者都承担数据持久化操作职责

- dao (Data Access Object)：JPA规范中数据库操作的抽象层
- mapper：MyBatis框架中SQL映射的接口定义
技术栈对应关系：

使用JPA时 → 建议保留dao目录
使用MyBatis时 → 建议使用mapper目录

![img_1.png](img_1.png)



- 在hotfix分支上修改readme文件





- 在 ShopMapper 中定义 findShopById 方法（通过注解或 XML）。
- 创建 ShopImage 实体类和 ShopImageMapper，并在数据库中创建 shop_image 表。
- 在 ShopImageMapper 中定义 findImagesByShopId 方法，用于根据商家 ID 查询图片。

当用户在前端分页列表中点击某个商家时，
前端会获取该商家的 ID，然后向后端发送一个请求，
请求该商家的详细信息。后端接收到这个 ID 后，
调用 findShopById 方法从数据库中查询对应的商家数据，再返回给前端，前端拿到数据后渲染详情页。















# 本地系统存储图片并在数据库中存储引用的方式

## 1. 总体架构

本地存储图片并在数据库中记录引用主要包含三个核心部分：
1. 文件系统中的图片存储
2. 数据库中的图片元数据记录
3. 连接两者的路径引用机制

## 2. 图片存储在文件系统

### 2.1 目录结构设计

```
项目根目录/
  ├── public/                # 公开可访问的静态资源
  │   └── images/            # 图片资源根目录
  │       └── shops/         # 商家相关图片
  │           ├── 1/         # 商家ID为1的图片目录
  │           │   ├── main.jpg       # 主图
  │           │   ├── food1.jpg      # 菜品图片
  │           │   └── exterior.jpg   # 门店外观
  │           ├── 2/
  │           └── ...
```

### 2.2 文件命名规范

采用以下命名规范确保文件组织合理：
1. **基于类型命名**：`[类型]_[序号].[扩展名]`，如`food_1.jpg`
2. **基于时间戳命名**：`[shop_id]_[timestamp].[扩展名]`，如`1_1630456789.jpg`
3. **使用UUID命名**：`[类型]_[UUID].[扩展名]`，如`exterior_a1b2c3d4.jpg`

## 3. 数据库存储设计

### 3.1 shop_image表结构

```sql
CREATE TABLE shop_image (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT NOT NULL COMMENT '关联的商家ID',
    image_path VARCHAR(255) NOT NULL COMMENT '图片相对路径',
    image_type VARCHAR(50) COMMENT '图片类型：主图/菜品/环境/外观等',
    description TEXT COMMENT '图片描述',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    is_main TINYINT(1) DEFAULT 0 COMMENT '是否为主图',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES shop(id),
    INDEX (shop_id)
);
```

### 3.2 存储路径的两种方式

1. **相对路径**：只存储相对于静态资源目录的路径
   ```sql
   INSERT INTO shop_image (shop_id, image_path) 
   VALUES (1, 'shops/1/main.jpg');
   ```

2. **完整URL**：存储完整的访问URL（包含域名）
   ```sql
   INSERT INTO shop_image (shop_id, image_path) 
   VALUES (1, 'https://example.com/images/shops/1/main.jpg');
   ```

## 4. 图片上传与存储实现

### 4.1 后端处理流程

```java
@PostMapping("/{shopId}/upload")
public Result<ShopImage> uploadImage(
        @PathVariable Integer shopId,
        @RequestParam("image") MultipartFile file,
        @RequestParam(value = "type", required = false) String imageType,
        @RequestParam(value = "description", required = false) String description) {
    
    // 1. 验证商家ID是否存在
    Shop shop = shopService.getById(shopId);
    if (shop == null) {
        return Result.error("商家不存在");
    }
    
    try {
        // 2. 确保目录存在
        String dirPath = "public/images/shops/" + shopId;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 3. 生成文件名
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 4. 保存文件
        String filePath = dirPath + "/" + fileName;
        File dest = new File(filePath);
        file.transferTo(dest);
        
        // 5. 保存数据库记录
        ShopImage shopImage = new ShopImage();
        shopImage.setShopId(shopId);
        shopImage.setImagePath("images/shops/" + shopId + "/" + fileName);
        shopImage.setImageType(imageType);
        shopImage.setDescription(description);
        
        shopImageMapper.insert(shopImage);
        
        return Result.success(shopImage);
    } catch (Exception e) {
        return Result.error("图片上传失败: " + e.getMessage());
    }
}
```

### 4.2 配置静态资源访问

在 Spring Boot 中配置静态资源访问：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 图片上传的路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:public/images/");
    }
}
```

## 5. 前端获取和展示图片

### 5.1 前端API调用示例

```javascript
async getShopImages(shopId) {
  try {
    const response = await axios.get(`/api/shop/${shopId}/images`);
    return response.data;
  } catch (error) {
    console.error('获取商家图片失败:', error);
    return [];
  }
}
```

### 5.2 图片展示组件

```vue
<template>
  <div class="shop-images">
    <div v-for="image in images" :key="image.id" class="image-item">
      <img 
        :src="getImageUrl(image.imagePath)" 
        :alt="image.description"
        @error="handleImageError"
      />
      <div class="image-description">{{ image.description }}</div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    images: Array
  },
  methods: {
    getImageUrl(path) {
      // 如果是完整URL则直接返回
      if (path.startsWith('http')) {
        return path;
      }
      // 否则拼接基础URL
      return `${process.env.VUE_APP_BASE_URL}/${path}`;
    },
    handleImageError(e) {
      // 图片加载失败时使用默认图片
      e.target.src = '/images/default-shop.jpg';
    }
  }
}
</script>
```

## 6. 高级功能与优化

### 6.1 图片压缩和调整

使用图片处理库（如Java的Thumbnailator或ImageMagick）进行图片处理：

```java
// 生成不同尺寸的图片
Thumbnails.of(file.getInputStream())
    .size(800, 600)
    .outputQuality(0.8)
    .toFile(new File(dirPath + "/" + fileName));

// 额外生成缩略图
Thumbnails.of(file.getInputStream())
    .size(200, 150)
    .outputQuality(0.7)
    .toFile(new File(dirPath + "/thumb_" + fileName));
```

### 6.2 图片分类管理

根据图片类型自动归类：

```java
// 按图片类型存入不同子目录
String typeDir = dirPath + "/" + (imageType == null ? "others" : imageType);
File typeDirFile = new File(typeDir);
if (!typeDirFile.exists()) {
    typeDirFile.mkdirs();
}

String filePath = typeDir + "/" + fileName;
```

### 6.3 图片批量上传

```java
@PostMapping("/{shopId}/batch-upload")
public Result<List<ShopImage>> batchUpload(
        @PathVariable Integer shopId,
        @RequestParam("images") MultipartFile[] files,
        @RequestParam("descriptions") String[] descriptions) {
    
    List<ShopImage> uploadedImages = new ArrayList<>();
    
    for (int i = 0; i < files.length; i++) {
        // 处理单个图片上传
        // ...保存逻辑与单个上传相似
    }
    
    return Result.success(uploadedImages);
}
```

## 7. 安全与性能注意事项

### 7.1 安全措施

1. **文件类型验证**：
   ```java
   String contentType = file.getContentType();
   if (contentType == null || !contentType.startsWith("image/")) {
       return Result.error("只能上传图片文件");
   }
   ```

2. **文件大小限制**：
   ```java
   if (file.getSize() > 5 * 1024 * 1024) { // 5MB
       return Result.error("图片大小不能超过5MB");
   }
   ```

3. **路径遍历防护**：
   ```java
   if (originalFileName.contains("..")) {
       return Result.error("文件名不合法");
   }
   ```

### 7.2 性能优化

1. **使用数据库索引**：为`shop_id`字段创建索引
2. **实现图片懒加载**：在前端只加载可见区域的图片
3. **批量查询优化**：一次性查询某商家的所有图片

## 8. 总结

本地存储图片并在数据库中记录引用是一种平衡存储效率和查询效率的好方法：

1. **文件系统**存储实际图片文件，充分利用文件系统的高效读写特性
2. **数据库**存储图片元数据和路径引用，便于关联查询和管理
3. **合理的目录结构**和命名规范确保图片井然有序
4. **安全防护措施**避免安全隐患
5. **性能优化**确保系统高效运行

这种方式适合中小型应用，成本低且易于实现和维护，同时具有良好的扩展性。
# 项目中调用后端图片信息的分析

我来分析项目中调用后端图片信息的关键位置和方式。

## 1. 前端服务层调用

在前端架构中，图片信息的获取主要通过`ShopService.js`中的服务方法进行：

### 1.1 获取商家详情时获取图片

```javascript
// SE_front_end/vue_pj/src/services/ShopService.js
async getShopDetails(shopId) {
  try {
    // 这里调用后端接口获取商家详情，包含图片信息
    const response = await axios.get(`${API_URL}/shop/${shopId}/detail`);
    return response.data;
  } catch (error) {
    console.error('获取商家详情失败:', error);
    throw error;
  }
}
```

这个方法会从后端获取完整的商家信息，包括关联的图片数据。后端会返回包含`shop`和`images`两个键的对象，其中`images`是该商家的所有图片信息数组。

### 1.2 获取首页推荐商家列表时获取图片

```javascript
// SE_front_end/vue_pj/src/services/ShopService.js
async getRecommendedShops(count = 4) {
  try {
    // 调用商家列表接口，后端会同时返回图片信息
    const response = await axios.get(`${API_URL}/shop/list`, {
      params: { 
        pageCurrent: 1, 
        pageSize: count 
      }
    });
    return response.data;
  } catch (error) {
    console.error('获取推荐商家失败:', error);
    throw error;
  }
}
```

### 1.3 商家搜索时获取图片

```javascript
// SE_front_end/vue_pj/src/services/ShopService.js
async searchShops(queryParams) {
  try {
    // 清理参数，发送搜索请求
    // ...
    const response = await axios.get(`${API_URL}/shop/search`, { params });
    // 后端返回的结果中包含商家和对应的图片信息
    return response.data;
  } catch (error) {
    // ...错误处理
  }
}
```

## 2. 前端组件中使用图片信息

### 2.1 商家详情页面

```vue
// SE_front_end/vue_pj/src/views/ShopDetail.vue
<template>
  <!-- ... -->
  <!-- 精选图片轮播 -->
  <div class="featured-images">
    <h3>精选展示</h3>
    <el-carousel :interval="4000" type="card" height="300px">
      <el-carousel-item v-for="(image, index) in images" :key="index">
        <div class="carousel-item">
          <img :src="image.imageUrl" :alt="image.description || '商家图片'" class="carousel-image">
          <div class="image-description" v-if="image.description">{{ image.description }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
  <!-- ... -->
</template>

<script>
export default {
  // ...
  setup() {
    // ...
    const loadShopDetails = async () => {
      try {
        const response = await ShopService.getShopDetails(shopId);
        // 后端返回数据格式: { shop: {...}, images: [...] }
        if (response && response.shop) {
          shop.value = response.shop;
          images.value = response.images || [];
        }
      } catch (error) {
        console.error('获取商家详情失败:', error);
      }
    };
    // ...
  }
}
</script>
```

### 2.2 首页商家列表

```vue
// SE_front_end/vue_pj/src/views/HomePage.vue
<template>
  <!-- ... -->
  <div v-for="item in leftColumnItems" :key="item.id" class="card" @click="navigateToShop(item.id)">
    <img :src="item.image" :alt="item.title" class="card-image">
    <!-- ... -->
  </div>
  <!-- ... -->
</template>

<script>
export default {
  // ...
  setup() {
    // ...
    const loadRecommendedShops = async () => {
      try {
        const response = await ShopService.getRecommendedShops(8);
        if (response.code === 1 && response.data) {
          const shops = response.data;
          
          // 处理左侧商家信息
          leftColumnItems.value = shops.slice(0, half).map(item => {
            const shop = item.shop;
            const images = item.images || [];
            
            // 优先使用数据库中的图片，如果没有则使用分类对应的默认图片
            let imageUrl = categoryImages.default;
            if (images && images.length > 0) {
              // 使用第一张图片作为封面
              imageUrl = images[0].imageUrl;
            } else if (shop.categoryName || (shop.category && shop.category.name)) {
              // 使用分类对应的默认图片
              const categoryName = shop.categoryName || shop.category?.name;
              imageUrl = categoryImages[categoryName] || categoryImages.default;
            }
            
            return {
              id: shop.id,
              image: imageUrl,
              // ...其他信息
            };
          });
          // ...处理右侧商家
        }
      } catch (error) {
        // ...错误处理
      }
    };
    // ...
  }
}
</script>
```

### 2.3 商家搜索结果页

```vue
// SE_front_end/vue_pj/src/views/ShopSearch.vue
<template>
  <!-- ... -->
  <div class="shop-list">
    <div v-for="shop in shops" :key="shop.id" class="shop-card" @click="viewShopDetails(shop.id)">
      <div class="shop-avatar">
        <img :src="getShopImage(shop)" alt="店铺图片" />
      </div>
      <!-- ... -->
    </div>
  </div>
  <!-- ... -->
</template>

<script>
export default {
  // ...
  setup() {
    // ...
    // 根据商家获取图片
    const getShopImage = (shop) => {
      // 获取商家对应的图片
      if (shop.images && shop.images.length > 0) {
        return shop.images[0].imageUrl;
      }
      
      // 如果没有图片，根据分类返回默认图片
      const categoryName = shop.categoryName || shop.category?.name;
      const categoryImages = {
        '火锅': 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
        // ...其他分类图片
      };
      
      return categoryImages[categoryName] || categoryImages.default;
    };
    // ...
  }
}
</script>
```

## 3. 后端提供图片信息的接口

### 3.1 商家详情接口

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/controller/ShopController.java
@GetMapping("/{shopId}/detail")
public Map<String, Object> getShopDetails(@PathVariable("shopId") Long shopId) {
    // 调用service层获取商家详情和图片
    return shopService.getShopDetails(shopId);
}
```

### 3.2 服务层实现

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/service/impl/ShopServiceImpl.java
@Override
public Map<String, Object> getShopDetails(Long shopId) {
    Map<String, Object> result = new HashMap<>();

    // 查询商家基本信息
    Shop shop = shopMapper.findShopById(shopId);
    result.put("shop", shop);

    // 查询商家图片
    List<ShopImage> images = shopImageMapper.findImagesByShopId(shopId);
    result.put("images", images);

    return result;
}
```

### 3.3 商家列表接口

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/controller/ShopController.java
@GetMapping("/list")
public Result<List<Map<String, Object>>> getShopList(
        @RequestParam(defaultValue = "1") Integer pageCurrent,
        @RequestParam(defaultValue = "4") Integer pageSize) {
    List<Shop> shops = shopService.showShops(pageCurrent, pageSize);
    
    // 为每个商家获取图片信息
    List<Map<String, Object>> result = shops.stream().map(shop -> {
        Map<String, Object> shopData = new HashMap<>();
        shopData.put("shop", shop);
        shopData.put("images", shopService.getShopImages(shop.getId()));
        return shopData;
    }).collect(java.util.stream.Collectors.toList());
    
    return Result.success(result);
}
```

### 3.4 搜索接口

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/controller/ShopController.java
@GetMapping("/search")
public Result<List<Map<String, Object>>> search(ShopPageQueryDTO shopPageQueryDTO) {
    try {
        // ...参数处理
        
        // 执行搜索
        List<Shop> shopList = shopService.searchShops(shopPageQueryDTO);
        
        // 为每个商家获取图片信息
        List<Map<String, Object>> result = shopList.stream().map(shop -> {
            Map<String, Object> shopData = new HashMap<>();
            shopData.put("shop", shop);
            shopData.put("images", shopService.getShopImages(shop.getId()));
            return shopData;
        }).collect(java.util.stream.Collectors.toList());
        
        return Result.success(result);
    } catch (Exception e) {
        // ...错误处理
    }
}
```

### 3.5 获取商家图片的服务方法

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/service/impl/ShopServiceImpl.java
@Override
public List<ShopImage> getShopImages(Integer shopId) {
    // 将Integer类型的shopId转换为Long类型再传递
    return shopImageMapper.findImagesByShopId(shopId != null ? shopId.longValue() : null);
}
```

## 4. 数据访问层查询

```java
// xiaozhong_dianping/server/server-customer/src/main/java/com/fudan/xiaozhong_dianping/shop/mapper/ShopImageMapper.java
@Mapper
public interface ShopImageMapper {
    /**
     * 根据商家ID查询图片列表
     * @param shopId 商家ID
     * @return 图片列表
     */
    List<ShopImage> findImagesByShopId(@Param("shopId") Long shopId);
}
```

```xml
<!-- xiaozhong_dianping/server/server-customer/src/main/resources/mapper/ShopImageMapper.xml -->
<select id="findImagesByShopId" parameterType="long" resultType="com.fudan.xiaozhong_dianping.shop.entity.ShopImage">
    SELECT * FROM shop_image WHERE shop_id = #{shopId}
</select>
```

## 5. 调用流程总结

从上面的分析，可以看出项目中调用后端图片信息的完整流程：

1. **用户交互触发**
   - 用户浏览首页、搜索商家或查看商家详情

2. **前端组件调用服务层**
   - `HomePage.vue` → `ShopService.getRecommendedShops()`
   - `ShopSearch.vue` → `ShopService.searchShops()`
   - `ShopDetail.vue` → `ShopService.getShopDetails()`

3. **前端服务层发起API请求**
   - `getRecommendedShops()` → `GET /shop/list`
   - `searchShops()` → `GET /shop/search`
   - `getShopDetails()` → `GET /shop/{shopId}/detail`

4. **后端控制器处理请求**
   - `ShopController.getShopList()`
   - `ShopController.search()`
   - `ShopController.getShopDetails()`

5. **后端服务层查询数据**
   - `ShopService.showShops()` + `ShopService.getShopImages()`
   - `ShopService.searchShops()` + `ShopService.getShopImages()`
   - `ShopService.getShopDetails()`

6. **数据访问层执行数据库查询**
   - `ShopMapper.showShops()` / `ShopMapper.searchShops()` / `ShopMapper.findShopById()`
   - `ShopImageMapper.findImagesByShopId()`

7. **前端接收并处理数据**
   - 处理商家基本信息和图片数据
   - 优先使用数据库图片，无图片时使用默认图片
   - 根据不同的页面组件显示图片

8. **图片渲染到页面**
   - 轮播图、商家卡片、详情图集等不同展示方式

这个流程确保了前端能够获取并展示后端存储的商家图片信息，实现了完整的图片管理和展示功能。



您说得对，我应该在正确的项目路径下创建资源目录。让我为您创建正确的目录结构。

```bash
mkdir -p xiaozhong_dianping/server/server-customer/src/main/resources/static/images/shops
```

```bash
mkdir -p xiaozhong_dianping/server/server-customer/src/main/resources/static/images/shops
```
看起来PowerShell在Windows系统上有一些路径创建的问题。让我们用另一种方式来创建目录结构。

```bash
cd xiaozhong_dianping/server/server-customer
```

```bash
mkdir -p src/main/resources/static/images/shops
```

```bash
mkdir -p "src/main/resources/static/images/shops"
```
看起来目录已经存在了，让我们确认一下目录结构。

```bash
ls -la src/main/resources/static/images
```

```bash
dir src/main/resources/static/images
```
很好，现在我们已经确认在正确的位置创建了 `shops` 目录。

根据您的项目需求，我建议在该目录下进一步创建以下子目录结构来分类存储不同类型的商家图片：

```bash
mkdir -p "src/main/resources/static/images/shops/environment" "src/main/resources/static/images/shops/food" "src/main/resources/static/images/shops/other"
```
看起来PowerShell在处理嵌套路径时遇到了一些问题。让我们分开创建这些目录：

```bash
mkdir "src\main\resources\static\images\shops\environment"
```

```bash
mkdir "src\main\resources\static\images\shops\food"
```

```bash
mkdir "src\main\resources\static\images\shops\other"
```

```bash
dir "src\main\resources\static\images\shops\"
```
很好！现在我们已经成功创建了所需的目录结构：
- `src/main/resources/static/images/shops/environment` - 用于存储店铺环境图片
- `src/main/resources/static/images/shops/food` - 用于存储招牌菜品图片
- `src/main/resources/static/images/shops/other` - 用于存储其他相关图片

现在我来回答您的问题：

## 商家图片存储方案

1. **图片存放位置**：
   - 路径：`xiaozhong_dianping/server/server-customer/src/main/resources/static/images/shops/`
   - 子目录结构：
     - `environment/` - 店铺环境图片
     - `food/` - 菜品图片
     - `other/` - 其他图片

2. **图片命名规则**：
   建议采用 `{shop_id}_{category}_{sequence}.{extension}` 格式
   例如：
   - `123_environment_01.jpg` - 商家ID为123的第1张环境图片
   - `123_food_01.png` - 商家ID为123的第1张菜品图片
   - `123_other_01.jpg` - 商家ID为123的第1张其他类型图片

3. **数据库记录方式**：
   根据您的 `shop_image` 表结构：
   ```sql
   CREATE TABLE shop_image (
       id INT AUTO_INCREMENT COMMENT '图片 ID（主键，自增）',
       shop_id INT NOT NULL COMMENT '所属商家 ID（关联 shop 表）',
       image_url TEXT NOT NULL COMMENT '图片存储 URL（支持多图）',
       description TEXT COMMENT '图片描述（如"招牌菜品"、"门店外观"）',
       PRIMARY KEY (id),
       FOREIGN KEY (shop_id) REFERENCES shop(id),
       INDEX shop_id (shop_id)
   )
   ```

   记录示例：
   ```
   id: 1
   shop_id: 123
   image_url: "/images/shops/environment/123_environment_01.jpg"
   description: "店铺环境"
   ```

4. **前端展示获取**：
   前端可以通过以下方式获取不同类型的图片：
   - 按店铺ID查询所有图片：`SELECT * FROM shop_image WHERE shop_id = ?`
   - 按类型筛选图片：`SELECT * FROM shop_image WHERE shop_id = ? AND description = ?`

5. **图片访问方式**：
   通过您的 WebConfig 配置，图片将可以通过以下URL访问：
   ```
   http://your-domain/images/shops/environment/123_environment_01.jpg
   ```

这种组织方式有以下优点：
- 清晰的分类结构，易于管理
- 通过文件名即可识别图片所属店铺和类型
- 支持通过数据库灵活查询不同类型的图片
- 与您现有的项目配置兼容

您可以根据实际需要调整这个方案。例如，如果商家数量非常多，还可以进一步按商家ID分目录存储，如 `shops/{shop_id}/environment/`。
