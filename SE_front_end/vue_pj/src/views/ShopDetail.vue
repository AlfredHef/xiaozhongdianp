<template>
  <div class="shop-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="!shop" class="no-data">
      <h2>未找到商家信息</h2>
      <el-button type="primary" @click="goBack">返回搜索页</el-button>
    </div>
    
    <div v-else class="shop-detail">
      <!-- 返回导航 -->
      <div class="navigation">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回搜索页</el-button>
      </div>
      
      <!-- 商家基本信息 -->
      <div class="shop-header">
        <div class="shop-title-area">
          <h1 class="shop-name">{{ shop.name }}</h1>
          <el-tag v-if="shop.category" size="medium" type="success">{{ shop.category.name || shop.categoryName }}</el-tag>
        </div>
        
        <div class="shop-rating">
          <el-rate v-model="shop.rating" disabled text-color="#ff9900" />
          <span class="rating-value">{{ shop.rating }}分</span>
        </div>
        
        <div class="price-info">
          <div class="price-item">
            <span class="price-label">价格区间</span>
            <span class="price-value">¥{{ shop.priceMin }} - ¥{{ shop.priceMax }}</span>
          </div>
          <div class="price-item">
            <span class="price-label">人均消费</span>
            <span class="price-value">¥{{ shop.averageCost }}</span>
          </div>
        </div>
        
        <div v-if="shop.description" class="shop-description">
          <h3>商家介绍</h3>
          <p>{{ shop.description }}</p>
        </div>
      </div>
      
      <!-- 商家联系信息 -->
      <div class="contact-info">
        <div class="info-item">
          <i class="el-icon-location"></i>
          <span>{{ shop.address || '暂无地址信息' }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-time"></i>
          <span>{{ shop.businessHours || '暂无营业时间信息' }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-phone"></i>
          <span>{{ shop.phone || '暂无联系电话' }}</span>
        </div>
      </div>
      
      <!-- 商家图片展示 -->
      <div class="shop-images-section">
        <h2>商家图片</h2>
        
        <div v-if="!images || images.length === 0" class="no-images">
          <el-empty description="暂无商家图片" />
        </div>
        
        <div v-else>
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
          
          <!-- 按类别分组展示图片 -->
          <div class="image-categories">
            <!-- 店面环境图片 -->
            <div v-if="getImagesByType('门店外观').length > 0 || getImagesByType('店内环境').length > 0" class="image-category">
              <h3>店铺环境</h3>
              <div class="image-grid">
                <div 
                  v-for="(image, index) in [...getImagesByType('门店外观'), ...getImagesByType('店内环境')]" 
                  :key="'env-'+index" 
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                  <img :src="image.imageUrl" :alt="image.description || '环境图片'">
                  <div class="image-tag">{{ image.description || '环境图片' }}</div>
                </div>
              </div>
            </div>
            
            <!-- 菜品图片 -->
            <div v-if="getImagesByType('招牌菜品').length > 0 || getImagesByType('特色美食').length > 0" class="image-category">
              <h3>特色菜品</h3>
              <div class="image-grid">
                <div 
                  v-for="(image, index) in [...getImagesByType('招牌菜品'), ...getImagesByType('特色美食')]" 
                  :key="'food-'+index" 
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                  <img :src="image.imageUrl" :alt="image.description || '菜品图片'">
                  <div class="image-tag">{{ image.description || '菜品图片' }}</div>
                </div>
              </div>
            </div>
            
            <!-- 其他图片 -->
            <div v-if="getImagesByType('其他').length > 0" class="image-category">
              <h3>其他图片</h3>
              <div class="image-grid">
                <div 
                  v-for="(image, index) in getImagesByType('其他')" 
                  :key="'other-'+index" 
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                  <img :src="image.imageUrl" :alt="image.description || '其他图片'">
                  <div class="image-tag">{{ image.description || '图片' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showViewer"
      :url-list="[previewUrl]"
      @close="closeViewer"
    />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElImageViewer } from 'element-plus';
import ShopService from '@/services/ShopService';

export default {
  name: 'ShopDetail',
  components: {
    ElImageViewer
  },
  
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    const shop = ref(null);
    const images = ref([]);
    const loading = ref(true);
    const showViewer = ref(false);
    const previewUrl = ref('');
    
    // 获取商家详情
    const loadShopDetails = async () => {
      const shopId = route.params.id;
      if (!shopId) {
        loading.value = false;
        return;
      }
      
      try {
        const response = await ShopService.getShopDetails(shopId);
        console.log('商家详情响应:', response);
        
        // 直接处理服务器返回的数据
        if (response && response.shop) {
          shop.value = response.shop;
          images.value = response.images || [];
          console.log('商家图片:', images.value);
        } else {
          console.error('商家详情数据格式不符合预期:', response);
        }
      } catch (error) {
        console.error('获取商家详情失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 返回搜索页
    const goBack = () => {
      router.push({ name: 'ShopSearch' });
    };
    
    // 按类型获取图片
    const getImagesByType = (type) => {
      if (!images.value || images.value.length === 0) return [];
      
      const typeMap = {
        '门店外观': ['门店外观', '店铺外观', '门面'],
        '店内环境': ['店内环境', '店内', '环境'],
        '招牌菜品': ['招牌菜品', '招牌菜', '特色菜'],
        '特色美食': ['特色美食', '菜品', '美食'],
        '其他': []
      };
      
      if (type === '其他') {
        // 返回不属于任何已知类别的图片
        const allKnownTypes = [].concat(...Object.values(typeMap).filter(arr => arr.length > 0));
        return images.value.filter(img => 
          !img.description || 
          !allKnownTypes.some(knownType => 
            img.description.includes(knownType)
          )
        );
      }
      
      // 返回匹配指定类型的图片
      return images.value.filter(img => 
        img.description && 
        typeMap[type].some(keyword => 
          img.description.includes(keyword)
        )
      );
    };
    
    // 预览图片
    const previewImage = (url) => {
      previewUrl.value = url;
      showViewer.value = true;
    };
    
    // 关闭预览
    const closeViewer = () => {
      showViewer.value = false;
    };
    
    onMounted(() => {
      loadShopDetails();
    });
    
    return {
      shop,
      images,
      loading,
      goBack,
      getImagesByType,
      showViewer,
      previewUrl,
      previewImage,
      closeViewer
    };
  }
};
</script>

<style scoped>
.shop-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.navigation {
  margin-bottom: 20px;
}

.loading, .no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.shop-header {
  margin-bottom: 30px;
  padding: 25px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.shop-title-area {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.shop-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  margin-right: 15px;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rating-value {
  margin-left: 8px;
  color: #ff9900;
  font-weight: bold;
  font-size: 18px;
}

.price-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.price-item {
  display: flex;
  flex-direction: column;
}

.price-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.price-value {
  font-weight: bold;
  color: #f56c6c;
  font-size: 18px;
}

.shop-description {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.shop-description h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.shop-description p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.contact-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.info-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.info-item i {
  margin-right: 8px;
  font-size: 20px;
  color: #409EFF;
}

.shop-images-section {
  margin-bottom: 40px;
}

.shop-images-section h2 {
  font-size: 22px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
}

.shop-images-section h3 {
  font-size: 18px;
  margin: 15px 0;
  color: #303133;
}

.featured-images {
  margin-bottom: 30px;
}

.carousel-item {
  position: relative;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.image-description {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 8px 15px;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.image-categories {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.image-category {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.grid-item {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 200px;
}

.grid-item:hover {
  transform: scale(1.03);
}

.grid-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-tag {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 5px 10px;
  font-size: 12px;
  text-align: center;
}

.no-images {
  padding: 40px;
  background-color: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}
</style> 