<template>
  <div class="shop-detail" v-loading="loading">
    <!-- 商家基本信息 -->
    <div class="shop-header">
      <el-breadcrumb class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>商家详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="basic-info">
        <div class="shop-title">
          <h1>{{ shop.name }}</h1>
          <div class="shop-rating">
            <el-rate
              v-model="shop.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
            <span class="review-count">({{ shop.reviewCount }}条评价)</span>
          </div>
        </div>
        
        <div class="shop-tags">
          <el-tag
            v-for="(tag, index) in shop.tags"
            :key="index"
            class="tag"
            size="small"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 商家详细信息 -->
    <el-row :gutter="20" class="detail-content">
      <el-col :span="16">
        <!-- 商家图片展示 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>商家图片</span>
            </div>
          </template>
          <el-carousel :interval="4000" type="card" height="400px">
            <el-carousel-item v-for="(image, index) in shop.images" :key="index">
              <el-image
                :src="image"
                fit="cover"
                :preview-src-list="shop.images"
              />
            </el-carousel-item>
          </el-carousel>
        </el-card>

        <!-- 商家介绍 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>商家介绍</span>
            </div>
          </template>
          <div class="shop-description">
            {{ shop.description }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 营业信息 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>营业信息</span>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="label">营业时间：</span>
              <span>{{ shop.businessHours }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span class="label">商家地址：</span>
              <span>{{ shop.address }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="label">联系电话：</span>
              <span>{{ shop.phone }}</span>
            </div>
            <div class="info-item">
              <el-icon><Money /></el-icon>
              <span class="label">人均消费：</span>
              <span>￥{{ shop.averagePrice }}</span>
            </div>
          </div>
        </el-card>

        <!-- 商家服务 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>商家服务</span>
            </div>
          </template>
          <div class="service-list">
            <div
              v-for="(service, index) in shop.services"
              :key="index"
              class="service-item"
            >
              <el-icon><Check /></el-icon>
              <span>{{ service }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Clock, Location, Phone, Money, Check } from '@element-plus/icons-vue'
import { getShopDetail, getShopRatings, getShopServices } from '@/api/shop'
import { ElMessage } from 'element-plus'

export default {
  name: 'ShopDetail',
  components: {
    Clock,
    Location,
    Phone,
    Money,
    Check
  },
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const shop = ref({
      name: '',
      rating: 0,
      reviewCount: 0,
      tags: [],
      images: [],
      description: '',
      businessHours: '',
      address: '',
      phone: '',
      averagePrice: 0,
      services: []
    })

    const fetchShopDetail = async () => {
      try {
        loading.value = true
        const shopId = route.params.id
        
        // 并行请求商家详情、评分和服务信息
        const [detailRes, ratingsRes, servicesRes] = await Promise.all([
          getShopDetail(shopId),
          getShopRatings(shopId),
          getShopServices(shopId)
        ])

        // 更新商家信息
        shop.value = {
          ...detailRes,
          ratings: ratingsRes,
          services: servicesRes
        }
      } catch (error) {
        console.error('获取商家详情失败:', error)
        ElMessage.error('获取商家信息失败，请重试')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      fetchShopDetail()
    })

    return {
      loading,
      shop
    }
  }
}
</script>

<style scoped>
.shop-detail {
  padding: 20px;
}

.shop-header {
  margin-bottom: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.basic-info {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.shop-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.shop-title h1 {
  margin: 0;
  font-size: 24px;
}

.shop-rating {
  display: flex;
  align-items: center;
}

.review-count {
  margin-left: 8px;
  color: #909399;
}

.shop-tags {
  margin-top: 10px;
}

.tag {
  margin-right: 8px;
}

.detail-content {
  margin-top: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}

.shop-description {
  line-height: 1.8;
  color: #606266;
}

.info-list {
  .info-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .el-icon {
      margin-right: 8px;
      color: #409EFF;
    }

    .label {
      color: #909399;
      margin-right: 8px;
    }
  }
}

.service-list {
  .service-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .el-icon {
      margin-right: 8px;
      color: #67C23A;
    }
  }
}

.el-carousel__item {
  .el-image {
    width: 100%;
    height: 100%;
  }
}
</style> 