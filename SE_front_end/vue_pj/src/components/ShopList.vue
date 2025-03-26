<template>
  <div class="shop-list">
    <!-- 排序工具栏 -->
    <div class="sort-toolbar">
      <el-radio-group v-model="sortType" @change="handleSortChange">
        <el-radio-button label="default">综合排序</el-radio-button>
        <el-radio-button label="rating">评分最高</el-radio-button>
        <el-radio-button label="price_low">人均最低</el-radio-button>
        <el-radio-button label="price_high">人均最高</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商家列表 -->
    <div class="shop-grid" v-loading="loading">
      <el-card
        v-for="shop in sortedShops"
        :key="shop.id"
        class="shop-card"
        @click="goToDetail(shop.id)"
      >
        <div class="shop-image">
          <el-image
            :src="shop.imageUrl"
            fit="cover"
            :preview-src-list="[shop.imageUrl]"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="shop-info">
          <h3 class="shop-name">{{ shop.name }}</h3>
          <div class="shop-rating">
            <el-rate
              v-model="shop.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
            <span class="review-count">({{ shop.reviewCount }}条评价)</span>
          </div>
          <div class="shop-tags">
            <el-tag
              v-for="(tag, index) in shop.tags"
              :key="index"
              size="small"
              class="shop-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="shop-price">
            <span class="price">￥{{ shop.averagePrice }}/人</span>
          </div>
          <div class="shop-address">
            <el-icon><Location /></el-icon>
            <span>{{ shop.address }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Location } from '@element-plus/icons-vue'

export default {
  name: 'ShopList',
  components: {
    Picture,
    Location
  },
  props: {
    shops: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const router = useRouter()
    const sortType = ref('default')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const total = ref(0)

    // 排序后的商家列表
    const sortedShops = computed(() => {
      const shops = [...props.shops]
      switch (sortType.value) {
        case 'rating':
          return shops.sort((a, b) => b.rating - a.rating)
        case 'price_low':
          return shops.sort((a, b) => a.averagePrice - b.averagePrice)
        case 'price_high':
          return shops.sort((a, b) => b.averagePrice - a.averagePrice)
        default:
          return shops
      }
    })

    const handleSortChange = (value) => {
      sortType.value = value
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
    }

    const goToDetail = (shopId) => {
      router.push(`/shop/${shopId}`)
    }

    return {
      sortType,
      currentPage,
      pageSize,
      total,
      sortedShops,
      handleSortChange,
      handleSizeChange,
      handleCurrentChange,
      goToDetail
    }
  }
}
</script>

<style scoped>
.shop-list {
  padding: 20px;
}

.sort-toolbar {
  margin-bottom: 20px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.shop-card {
  cursor: pointer;
  transition: all 0.3s;
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.shop-image {
  height: 200px;
  overflow: hidden;
}

.shop-image .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.shop-info {
  padding: 15px;
}

.shop-name {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: bold;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.review-count {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.shop-tags {
  margin-bottom: 10px;
}

.shop-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.shop-price {
  margin-bottom: 10px;
  color: #f56c6c;
  font-weight: bold;
}

.shop-address {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 