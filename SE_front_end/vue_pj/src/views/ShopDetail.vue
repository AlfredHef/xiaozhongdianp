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
      <!-- 商家基本信息 -->
      <div class="shop-header">
        <h1 class="shop-name">{{ shop.name }}</h1>
        <div class="shop-rating">
          <el-rate v-model="shop.rating" disabled text-color="#ff9900" />
          <span>{{ shop.rating }}分</span>
        </div>
        <div class="shop-price">
          <span class="price-label">人均消费：</span>
          <span class="price-value">¥{{ shop.averageCost }}</span>
          <span class="price-range">价格区间：¥{{ shop.priceMin }} - ¥{{ shop.priceMax }}</span>
        </div>
      </div>
      
      <!-- 商家图片展示 -->
      <div class="shop-images" v-if="images && images.length > 0">
        <h2>商家图片</h2>
        <el-carousel :interval="4000" type="card" height="300px">
          <el-carousel-item v-for="(image, index) in images" :key="index">
            <img :src="image.imageUrl" :alt="'商家图片 ' + (index + 1)" class="carousel-image">
          </el-carousel-item>
        </el-carousel>
      </div>
      <div v-else class="shop-images empty-images">
        <h2>商家图片</h2>
        <div class="no-images">
          <el-empty description="暂无商家图片" />
        </div>
      </div>
      
      <!-- 商家详细信息 -->
      <div class="shop-info-card">
        <h2>商家信息</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商家名称">{{ shop.name }}</el-descriptions-item>
          <el-descriptions-item label="商家地址">{{ shop.address }}</el-descriptions-item>
          <el-descriptions-item label="营业时间">{{ shop.businessHours }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ shop.phone }}</el-descriptions-item>
          <el-descriptions-item label="商家分类">{{ shop.category ? shop.category.name : '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="人均消费">¥{{ shop.averageCost }}</el-descriptions-item>
          <el-descriptions-item label="价格区间">¥{{ shop.priceMin }} - ¥{{ shop.priceMax }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 返回按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="goBack">返回搜索页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ShopService from '@/services/ShopService';

export default {
  name: 'ShopDetail',
  
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    const shop = ref(null);
    const images = ref([]);
    const loading = ref(true);
    
    // 获取商家详情
    const loadShopDetails = async () => {
      const shopId = route.params.id;
      if (!shopId) {
        loading.value = false;
        return;
      }
      
      try {
        const response = await ShopService.getShopDetails(shopId);
        if (response.code === 200 && response.data) {
          shop.value = response.data.shop;
          images.value = response.data.images || [];
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
    
    onMounted(() => {
      loadShopDetails();
    });
    
    return {
      shop,
      images,
      loading,
      goBack
    };
  }
};
</script>

<style scoped>
.shop-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
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
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.shop-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 10px;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.shop-rating span {
  margin-left: 8px;
  color: #ff9900;
  font-weight: bold;
}

.shop-price {
  font-size: 16px;
}

.price-label {
  color: #606266;
}

.price-value {
  font-weight: bold;
  color: #f56c6c;
  margin-right: 15px;
}

.price-range {
  color: #606266;
}

.shop-images {
  margin-bottom: 30px;
}

.shop-images h2 {
  margin-bottom: 15px;
  font-size: 20px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.empty-images {
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  padding: 20px;
}

.no-images {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shop-info-card {
  margin-bottom: 30px;
}

.shop-info-card h2 {
  margin-bottom: 15px;
  font-size: 20px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style> 