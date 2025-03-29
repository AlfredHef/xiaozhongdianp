<template>
  <div class="shop-search-container">
    <div class="search-bar">
      <div class="search-input">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索商家，如'火锅'、'奶茶'、'炸鸡'" 
          @keyup.enter="searchShops"
          clearable>
          <template #suffix>
            <el-button :icon="Search" circle @click="searchShops"></el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 搜索历史记录 -->
      <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
        <div class="history-header">
          <span>搜索历史</span>
          <el-button type="text" @click="clearSearchHistory">清空</el-button>
        </div>
        <div class="history-list">
          <span 
            v-for="(item, index) in searchHistory" 
            :key="index" 
            class="history-item" 
            @click="useHistoryItem(item.keyword)">
            {{ item.keyword }}
          </span>
        </div>
      </div>
    </div>

    <!-- 筛选面板 -->
    <div class="filter-panel">
      <div class="filter-section">
        <div class="filter-title">商家评分</div>
        <div class="filter-options">
          <el-checkbox-group v-model="selectedRatings">
            <el-checkbox label="5.0" @change="applyFilter">5.0分以上</el-checkbox>
            <el-checkbox label="4.5" @change="applyFilter">4.5分以上</el-checkbox>
            <el-checkbox label="4.0" @change="applyFilter">4.0分以上</el-checkbox>
            <el-checkbox label="3.5" @change="applyFilter">3.5分以上</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">价格区间</div>
        <div class="filter-options">
          <el-checkbox-group v-model="selectedPrices">
            <el-checkbox label="0-50" @change="applyFilter">￥0-50</el-checkbox>
            <el-checkbox label="50-100" @change="applyFilter">￥50-100</el-checkbox>
            <el-checkbox label="100-200" @change="applyFilter">￥100-200</el-checkbox>
            <el-checkbox label="200+" @change="applyFilter">￥200以上</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">人均消费</div>
        <div class="filter-options">
          <el-checkbox-group v-model="selectedAverageCosts">
            <el-checkbox label="0-50" @change="applyFilter">￥0-50/人</el-checkbox>
            <el-checkbox label="50-100" @change="applyFilter">￥50-100/人</el-checkbox>
            <el-checkbox label="100-200" @change="applyFilter">￥100-200/人</el-checkbox>
            <el-checkbox label="200+" @change="applyFilter">￥200以上/人</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">排序方式</div>
        <div class="filter-options">
          <el-radio-group v-model="sortBy" @change="applyFilter">
            <el-radio label="default">综合排序</el-radio>
            <el-radio label="rating_desc">评分最高</el-radio>
            <el-radio label="average_cost_asc">人均消费最低</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-actions">
        <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="shops.length === 0" class="no-results">
        <h3>暂无符合条件的商家</h3>
      </div>
      <div v-else class="shop-list">
        <div 
          v-for="shop in shops" 
          :key="shop.id" 
          class="shop-card" 
          @click="viewShopDetails(shop.id)">
          <div class="shop-avatar">
            <img :src="defaultImage" alt="店铺图片" />
          </div>
          <div class="shop-info">
            <h3 class="shop-name">{{ shop.name || '未命名商家' }}</h3>
            <div class="shop-rating">
              <el-rate 
                :model-value="shop.rating || 0" 
                disabled 
                text-color="#ff9900" />
              <span>{{ shop.rating || '暂无评分' }}</span>
            </div>
            <div class="shop-meta">
              <el-tag size="small" type="success" v-if="shop.category && shop.category.name">{{ shop.category.name }}</el-tag>
              <el-tag size="small" type="success" v-else-if="shop.categoryName">{{ shop.categoryName }}</el-tag>
              <el-tag size="small" type="warning">人均￥{{ shop.averageCost || '未知' }}</el-tag>
              <el-tag size="small" type="info">￥{{ shop.priceMin || '0' }}-￥{{ shop.priceMax || '0' }}</el-tag>
            </div>
            <div class="shop-address"><i class="el-icon-location"></i> {{ shop.address || '暂无地址信息' }}</div>
            <div class="shop-hours"><i class="el-icon-time"></i> {{ shop.businessHours || '暂无营业时间信息' }}</div>
            <div class="shop-description" v-if="shop.description">{{ shop.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import ShopService from '@/services/ShopService';
import AuthService from '@/services/AuthService';

export default {
  name: 'ShopSearch',
  
  setup() {
    const router = useRouter();
    
    // 搜索相关
    const searchQuery = ref('');
    const shops = ref([]);
    const loading = ref(false);
    const searchHistory = ref([]);
    const showSearchHistory = ref(false);
    const defaultImage = 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png';
    
    // 筛选相关
    const selectedRatings = ref([]);
    const selectedPrices = ref([]);
    const selectedAverageCosts = ref([]);
    const sortBy = ref('default');
    
    // 用户信息
    const user = AuthService.getUser();
    const userId = user?.id || null; // 添加可选链操作符，确保安全访问
    
    console.log('当前用户信息:', user);
    
    // 监听搜索框点击，显示历史记录
    watch(searchQuery, () => {
      if (searchQuery.value === '') {
        showSearchHistory.value = true;
        loadSearchHistory();
      } else {
        showSearchHistory.value = false;
      }
    });
    
    // 页面加载时获取所有商家和搜索历史
    onMounted(() => {
      loadShops();
      loadSearchHistory();
    });
    
    // 加载商家列表
    const loadShops = async () => {
      loading.value = true;
      try {
        const response = await ShopService.getShops(1, 20);
        if (response.code === 1 && response.data) {
          shops.value = response.data;
        }
      } catch (error) {
        console.error('加载商家失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 加载搜索历史
    const loadSearchHistory = async () => {
      if (!userId) return;
      
      try {
        const response = await ShopService.getSearchHistory(userId);
        if (response.code === 1 && response.data) {
          searchHistory.value = response.data;
        }
      } catch (error) {
        console.error('加载搜索历史失败:', error);
      }
    };
    
    // 搜索商家
    const searchShops = async () => {
      if (!searchQuery.value.trim()) {
        loadShops();
        return;
      }
      
      loading.value = true;
      showSearchHistory.value = false;
      
      try {
        // 基础搜索参数
        const queryParams = {
          name: searchQuery.value,
          pageSize: 10,
          pageCurrent: 1,
          sortBy: sortBy.value
        };
        
        // 只有在用户已登录时才添加userId
        if (userId) {
          queryParams.userId = userId;
        }
        
        // 添加评分筛选
        if (selectedRatings.value.length > 0) {
          queryParams.minRating = Math.min(...selectedRatings.value.map(Number));
        }
        
        // 添加价格筛选
        if (selectedPrices.value.length > 0) {
          for (const priceRange of selectedPrices.value) {
            const [min, max] = priceRange.split('-');
            if (!queryParams.minPrice || Number(min) < queryParams.minPrice) {
              queryParams.minPrice = Number(min);
            }
            if (max !== '+' && (!queryParams.maxPrice || Number(max) > queryParams.maxPrice)) {
              queryParams.maxPrice = Number(max);
            } else if (max === '+' && !queryParams.maxPrice) {
              queryParams.maxPrice = 1000; // 设置一个较大的最大值
            }
          }
        }
        
        // 添加人均消费筛选
        if (selectedAverageCosts.value.length > 0) {
          for (const costRange of selectedAverageCosts.value) {
            const [min, max] = costRange.split('-');
            if (!queryParams.minAverageCost || Number(min) < queryParams.minAverageCost) {
              queryParams.minAverageCost = Number(min);
            }
            if (max !== '+' && (!queryParams.maxAverageCost || Number(max) > queryParams.maxAverageCost)) {
              queryParams.maxAverageCost = Number(max);
            } else if (max === '+' && !queryParams.maxAverageCost) {
              queryParams.maxAverageCost = 1000; // 设置一个较大的最大值
            }
          }
        }
        
        console.log('发送搜索请求，最终参数:', queryParams);
        const response = await ShopService.searchShops(queryParams);
        console.log('搜索结果:', response); // 添加日志
        
        if (response && response.code === 1 && response.data) {
          shops.value = response.data;
          console.log('更新后的商家列表:', shops.value); // 添加日志
        } else {
          console.error('搜索响应格式不符合预期:', response);
          shops.value = [];
        }
      } catch (error) {
        console.error('搜索商家失败:', error);
        shops.value = [];
      } finally {
        loading.value = false;
      }
    };
    
    // 使用历史记录项
    const useHistoryItem = (keyword) => {
      searchQuery.value = keyword;
      searchShops();
    };
    
    // 清空搜索历史
    const clearSearchHistory = () => {
      searchHistory.value = [];
    };
    
    // 应用筛选
    const applyFilter = () => {
      searchShops();
    };
    
    // 查看商家详情
    const viewShopDetails = (shopId) => {
      router.push({ name: 'ShopDetail', params: { id: shopId } });
    };
    
    // 重置筛选条件
    const resetFilters = () => {
      selectedRatings.value = [];
      selectedPrices.value = [];
      selectedAverageCosts.value = [];
      sortBy.value = 'default';
      searchShops();
    };
    
    return {
      Search,
      searchQuery,
      shops,
      loading,
      searchHistory,
      showSearchHistory,
      selectedRatings,
      selectedPrices,
      selectedAverageCosts,
      sortBy,
      searchShops,
      useHistoryItem,
      clearSearchHistory,
      applyFilter,
      viewShopDetails,
      resetFilters,
      defaultImage
    };
  }
};
</script>

<style scoped>
.shop-search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  position: relative;
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: white;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 10px;
  z-index: 100;
}

.history-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  padding: 4px 10px;
  background-color: #f5f7fa;
  border-radius: 16px;
  cursor: pointer;
  font-size: 12px;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.filter-section {
  flex: 1;
  min-width: 250px;
}

.filter-actions {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.filter-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.shop-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.shop-card {
  display: flex;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  background-color: #fff;
  margin-bottom: 10px;
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.shop-avatar {
  width: 120px;
  height: 120px;
  margin-right: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.shop-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.shop-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.shop-rating span {
  margin-left: 5px;
  color: #ff9900;
  font-weight: bold;
}

.shop-meta {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.shop-address, .shop-hours {
  font-size: 13px;
  color: #606266;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.shop-address i, .shop-hours i {
  margin-right: 5px;
  font-size: 14px;
}

.shop-description {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.loading, .no-results {
  padding: 30px;
  text-align: center;
}
</style> 