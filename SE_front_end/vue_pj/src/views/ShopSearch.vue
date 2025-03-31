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

    <!-- 搜索结果统计 -->
    <div class="search-summary" v-if="!loading && searchQuery.trim() !== ''">
      <div class="result-count">
        找到 <strong>{{ shops.length }}</strong> 条结果
      </div>
      <div class="active-filters">
        <span v-if="selectedRating">评分：{{ selectedRating }}分以上</span>
        <span v-if="selectedPrice">价格：{{ formatPriceRange(selectedPrice) }}</span>
        <span v-if="selectedAverageCost">人均：{{ formatPriceRange(selectedAverageCost) }}/人</span>
        <span v-if="sortBy !== 'default'">
          排序：{{ 
            sortBy === 'rating_desc' ? '评分最高' : 
            sortBy === 'average_cost_asc' ? '人均消费最低' : '综合排序' 
          }}
        </span>
      </div>
    </div>

    <!-- 筛选面板 -->
    <div class="filter-panel">
      <div class="filter-section">
        <div class="filter-title">商家评分</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedRating" @change="applyFilter">
            <el-radio label="">全部</el-radio>
            <el-radio label="5.0">5.0分以上</el-radio>
            <el-radio label="4.5">4.5分以上</el-radio>
            <el-radio label="4.0">4.0分以上</el-radio>
            <el-radio label="3.5">3.5分以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">价格区间</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedPrice" @change="applyFilter">
            <el-radio label="">全部</el-radio>
            <el-radio label="0-50">￥0-50</el-radio>
            <el-radio label="50-100">￥50-100</el-radio>
            <el-radio label="100-200">￥100-200</el-radio>
            <el-radio label="200+">￥200以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">人均消费</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedAverageCost" @change="applyFilter">
            <el-radio label="">全部</el-radio>
            <el-radio label="0-50">￥0-50/人</el-radio>
            <el-radio label="50-100">￥50-100/人</el-radio>
            <el-radio label="100-200">￥100-200/人</el-radio>
            <el-radio label="200+">￥200以上/人</el-radio>
          </el-radio-group>
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
        <div class="filter-actions-left">
          <div class="filter-title">每页显示</div>
          <el-select v-model="pageSize" @change="handlePageSizeChange" size="small" placeholder="每页显示">
            <el-option :value="6" label="6条/页"></el-option>
            <el-option :value="9" label="9条/页"></el-option>
            <el-option :value="12" label="12条/页"></el-option>
            <el-option :value="18" label="18条/页"></el-option>
          </el-select>
        </div>
        <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="errorMessage" class="no-results error-message">
        <el-alert
          :title="errorMessage"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button class="retry-button" type="primary" size="small" @click="resetAndSearch">
          重置筛选条件并重试
        </el-button>
      </div>
      <div v-else-if="shops.length === 0" class="no-results">
        <h3>暂无符合条件的商家</h3>
        <p>尝试调整筛选条件或清空搜索关键词</p>
      </div>
      <div v-else class="shop-list">
        <div 
          v-for="shop in shops" 
          :key="shop.id" 
          class="shop-card" 
          @click="viewShopDetails(shop.id)">
          <div class="shop-avatar">
            <img :src="getShopImage(shop)" alt="店铺图片" />
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
    
    <!-- 分页控件 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @update:current-page="handlePageChange"
      ></el-pagination>
      <div class="pagination-info">
        共 {{ total }} 条记录，当前第 {{ currentPage }}/{{ totalPages }} 页
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed, onUnmounted } from 'vue';
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
    
    // 缓存所有商家数据，避免频繁请求
    const allShopsCache = ref([]);
    
    // 存储原始缓存数据
    const originalShopsCache = ref([]);
    
    // 筛选相关
    const selectedRatings = ref([]);
    const selectedPrices = ref([]);
    const selectedAverageCosts = ref([]);
    const selectedRating = ref('');
    const selectedPrice = ref('');
    const selectedAverageCost = ref('');
    const sortBy = ref('default');
    
    // 分页相关
    const currentPage = ref(1);
    const pageSize = ref(6);
    const total = ref(0);
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
    
    // 组件卸载标志
    const isUnmounted = ref(false);
    
    // 用户信息
    const user = AuthService.getUser();
    console.log('AuthService.getUser()返回的用户信息:', user);
    const userId = user?.id || null; // 添加可选链操作符，确保安全访问
    console.log('提取的用户ID:', userId);
    
    // 在setup中添加错误消息状态
    const errorMessage = ref('');
    
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
    
    // 组件卸载钩子
    onUnmounted(() => {
      isUnmounted.value = true;
      console.log('ShopSearch组件已卸载');
    });
    
    // 加载商家列表
    const loadShops = async () => {
      if (isUnmounted.value) return;
      
      loading.value = true;
      try {
        // 如果已有全部商家数据缓存，直接使用缓存数据
        if (originalShopsCache.value.length > 0) {
          console.log('使用缓存的商家数据，共', originalShopsCache.value.length, '条');
          allShopsCache.value = [...originalShopsCache.value]; // 复制原始数据
          total.value = allShopsCache.value.length;
          updatePagedShops();
          loading.value = false;
          return;
        }
        
        // 获取较大数量的商家数据
        console.log('从服务器请求商家数据');
        const response = await ShopService.getShops(1, 100);
        
        if (isUnmounted.value) return;
        
        if (response.code === 1 && response.data) {
          // 处理不同类型的响应数据结构
          if (Array.isArray(response.data)) {
            // 直接返回数组的情况
            originalShopsCache.value = response.data;
            allShopsCache.value = [...originalShopsCache.value];
            console.log(`直接获取到${allShopsCache.value.length}条商家数据`);
          } else if (response.data.records) {
            // 返回包含records字段的分页对象
            originalShopsCache.value = response.data.records;
            allShopsCache.value = [...originalShopsCache.value];
            console.log(`分页获取到${allShopsCache.value.length}条商家数据，总共${response.data.total || '未知'}条`);
          } else if (typeof response.data === 'object') {
            // 尝试从返回对象中找到数组
            const arrayFields = Object.entries(response.data)
              .find(([_, value]) => Array.isArray(value) && value.length > 0);
            
            if (arrayFields) {
              originalShopsCache.value = arrayFields[1];
              allShopsCache.value = [...originalShopsCache.value];
              console.log(`从对象字段获取到${allShopsCache.value.length}条商家数据`);
            } else {
              originalShopsCache.value = [response.data]; // 单个对象
              allShopsCache.value = [...originalShopsCache.value];
              console.log('获取到单个商家数据，转换为数组');
            }
          }
          
          // 设置总数据并进行前端分页
          if (allShopsCache.value.length > 0) {
            console.log(`成功缓存了 ${allShopsCache.value.length} 条商家数据`);
            total.value = allShopsCache.value.length;
            
            // 前端分页
            updatePagedShops();
          } else {
            console.warn('获取到的商家数据为空');
            shops.value = [];
            total.value = 0;
          }
        } else {
          console.error('获取商家列表失败:', response);
          shops.value = [];
          total.value = 0;
        }
      } catch (error) {
        if (isUnmounted.value) return;
        
        console.error('加载商家失败:', error);
        shops.value = [];
        total.value = 0;
      } finally {
        if (!isUnmounted.value) {
          loading.value = false;
        }
      }
    };
    
    // 根据当前页码和每页数量更新显示的商家数据
    const updatePagedShops = () => {
      if (allShopsCache.value.length === 0) return;
      
      const startIndex = (currentPage.value - 1) * pageSize.value;
      const endIndex = startIndex + pageSize.value;
      
      // 确保startIndex在有效范围内
      if (startIndex >= allShopsCache.value.length) {
        // 如果超出范围，调整到最后一页
        currentPage.value = Math.ceil(allShopsCache.value.length / pageSize.value);
        // 重新计算索引
        const newStartIndex = (currentPage.value - 1) * pageSize.value;
        shops.value = allShopsCache.value.slice(newStartIndex, newStartIndex + pageSize.value);
      } else {
        shops.value = allShopsCache.value.slice(startIndex, endIndex);
      }
      
      console.log(`显示${startIndex + 1}到${Math.min(endIndex, allShopsCache.value.length)}条记录，共${allShopsCache.value.length}条`);
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
      if (isUnmounted.value) return; // 如果组件已卸载，中止操作
      
      errorMessage.value = ''; // 清空之前的错误
      
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
          pageSize: 100, // 获取更多数据，在前端分页
          pageCurrent: 1, // 从第一页开始获取
          sortBy: sortBy.value
        };
        
        // 只有在用户已登录时才添加userId
        if (userId) {
          queryParams.userId = userId;
        }
        
        // 添加评分筛选
        if (selectedRating.value) {
          queryParams.minRating = Number(selectedRating.value);
        }
        
        // 添加价格筛选
        if (selectedPrice.value) {
          const [min, max] = selectedPrice.value.split('-');
          const minPrice = Number(min);
          if (!isNaN(minPrice)) {
            queryParams.minPrice = minPrice;
          }
          
          if (max && max.includes('+')) {
            // 处理"200+"这种情况，只设置下限，不设置上限
            // 或设置一个非常大的上限值
            console.log('价格筛选 - 设置最小值，无上限:', minPrice);
            delete queryParams.maxPrice; // 移除上限
          } else if (max) {
            const maxPrice = Number(max);
            if (!isNaN(maxPrice)) {
              queryParams.maxPrice = maxPrice;
              console.log('价格筛选 - 范围:', minPrice, '-', maxPrice);
            }
          }
        }
        
        // 添加人均消费筛选
        if (selectedAverageCost.value) {
          const [min, max] = selectedAverageCost.value.split('-');
          const minCost = Number(min);
          if (!isNaN(minCost)) {
            queryParams.minAverageCost = minCost;
          }
          
          if (max && max.includes('+')) {
            // 处理"200+"这种情况，只设置下限，不设置上限
            // 或设置一个非常大的上限值
            console.log('人均消费筛选 - 设置最小值，无上限:', minCost);
            delete queryParams.maxAverageCost; // 移除上限
          } else if (max) {
            const maxCost = Number(max);
            if (!isNaN(maxCost)) {
              queryParams.maxAverageCost = maxCost;
              console.log('人均消费筛选 - 范围:', minCost, '-', maxCost);
            }
          }
        }
        
        console.log('发送搜索请求，最终参数:', queryParams);
        const response = await ShopService.searchShops(queryParams);
        console.log('搜索结果:', response);
        
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        if (response.code === 0) {
          // 处理API返回的错误
          errorMessage.value = response.msg || '搜索时出现错误，请稍后重试';
          allShopsCache.value = [];
          shops.value = [];
          total.value = 0;
        } else if (response && response.code === 1) {
          // 重置当前页为第一页
          currentPage.value = 1;
          
          if (response.data && Array.isArray(response.data)) {
            // 直接返回数组的情况
            allShopsCache.value = response.data;
            total.value = response.data.length;
            console.log(`直接获取到${allShopsCache.value.length}条商家数据`);
          } else if (response.data && response.data.records) {
            // 返回包含records字段的分页对象
            allShopsCache.value = response.data.records;
            total.value = response.data.total || response.data.records.length;
            console.log(`分页获取到${allShopsCache.value.length}条商家数据，总共${total.value}条`);
          } else if (response.data) {
            // 其他情况，尝试适配
            allShopsCache.value = response.data || [];
            total.value = allShopsCache.value.length;
          } else {
            allShopsCache.value = [];
            shops.value = [];
            total.value = 0;
            console.error('响应中没有有效的数据');
          }
          
          // 更新显示的分页数据
          if (allShopsCache.value.length > 0) {
            updatePagedShops();
          } else {
            shops.value = [];
          }
          
          console.log('更新后的商家列表:', shops.value);
        } else {
          console.error('搜索响应格式不符合预期:', response);
          errorMessage.value = '获取数据格式错误，请联系管理员';
          allShopsCache.value = [];
          shops.value = [];
          total.value = 0;
        }
      } catch (error) {
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        console.error('搜索商家失败:', error);
        errorMessage.value = '搜索时发生意外错误，请稍后再试';
        allShopsCache.value = [];
        shops.value = [];
        total.value = 0;
      } finally {
        // 检查组件是否已卸载
        if (!isUnmounted.value) {
          loading.value = false;
          
          // 搜索后滚动到页面顶部
          window.scrollTo(0, 0);
        }
      }
    };
    
    // 页码变化处理
    const handlePageChange = (page) => {
      if (isUnmounted.value) return;
      
      console.log('页码变化，新页码:', page);
      currentPage.value = page;
      
      if (searchQuery.value.trim()) {
        // 有搜索关键词时，重新搜索
        searchShops();
      } else if (allShopsCache.value.length > 0) {
        // 没有搜索关键词且有缓存数据时，直接使用缓存数据分页
        updatePagedShops();
        // 滚动到页面顶部
        window.scrollTo(0, 0);
      }
    };
    
    // 使用历史记录项
    const useHistoryItem = (keyword) => {
      searchQuery.value = keyword;
      currentPage.value = 1; // 重置到第一页
      searchShops();
    };
    
    // 清空搜索历史
    const clearSearchHistory = async () => {
      if (isUnmounted.value) return;
      if (!userId) return;
      
      try {
        // 调用后端API清空搜索历史
        await ShopService.clearSearchHistory(userId);
        searchHistory.value = [];
      } catch (error) {
        console.error('清空搜索历史失败:', error);
      }
    };
    
    // 根据筛选条件对缓存数据进行过滤
    const filterCachedShops = () => {
      if (isUnmounted.value) return [];
      
      console.log('开始前端筛选，筛选条件:', {
        rating: selectedRating.value,
        price: selectedPrice.value,
        averageCost: selectedAverageCost.value,
        sortBy: sortBy.value
      });
      
      // 如果没有缓存数据，返回空数组
      if (allShopsCache.value.length === 0) {
        console.warn('缓存数据为空，无法进行筛选');
        return [];
      }
      
      // 对缓存数据进行深拷贝，避免影响原始数据
      let filteredShops = [...allShopsCache.value];
      
      // 应用评分筛选
      if (selectedRating.value) {
        const minRating = Number(selectedRating.value);
        if (!isNaN(minRating)) {
          console.log('应用评分筛选 >=', minRating);
          filteredShops = filteredShops.filter(shop => {
            const rating = Number(shop.rating);
            return !isNaN(rating) && rating >= minRating;
          });
        }
      }
      
      // 应用价格筛选
      if (selectedPrice.value) {
        const [min, max] = selectedPrice.value.split('-');
        const minPrice = Number(min);
        
        if (!isNaN(minPrice)) {
          console.log('应用价格下限筛选 >=', minPrice);
          filteredShops = filteredShops.filter(shop => {
            const shopMinPrice = Number(shop.priceMin);
            // 店铺最低价格大于等于筛选最低价格
            return !isNaN(shopMinPrice) && shopMinPrice >= minPrice;
          });
        }
        
        if (max && !max.includes('+')) {
          const maxPrice = Number(max);
          if (!isNaN(maxPrice)) {
            console.log('应用价格上限筛选 <=', maxPrice);
            filteredShops = filteredShops.filter(shop => {
              const shopMaxPrice = Number(shop.priceMax);
              // 店铺最高价格小于等于筛选最高价格
              return !isNaN(shopMaxPrice) && shopMaxPrice <= maxPrice;
            });
          }
        }
      }
      
      // 应用人均消费筛选
      if (selectedAverageCost.value) {
        const [min, max] = selectedAverageCost.value.split('-');
        const minCost = Number(min);
        
        if (!isNaN(minCost)) {
          console.log('应用人均消费下限筛选 >=', minCost);
          filteredShops = filteredShops.filter(shop => {
            const averageCost = Number(shop.averageCost);
            return !isNaN(averageCost) && averageCost >= minCost;
          });
        }
        
        if (max && !max.includes('+')) {
          const maxCost = Number(max);
          if (!isNaN(maxCost)) {
            console.log('应用人均消费上限筛选 <=', maxCost);
            filteredShops = filteredShops.filter(shop => {
              const averageCost = Number(shop.averageCost);
              return !isNaN(averageCost) && averageCost <= maxCost;
            });
          }
        }
      }
      
      // 应用排序
      if (sortBy.value !== 'default') {
        if (sortBy.value === 'rating_desc') {
          console.log('应用评分降序排序');
          filteredShops.sort((a, b) => {
            const ratingA = Number(a.rating) || 0;
            const ratingB = Number(b.rating) || 0;
            return ratingB - ratingA;
          });
        } else if (sortBy.value === 'average_cost_asc') {
          console.log('应用人均消费升序排序');
          filteredShops.sort((a, b) => {
            const costA = Number(a.averageCost) || 0;
            const costB = Number(b.averageCost) || 0;
            return costA - costB;
          });
        }
      }
      
      console.log(`筛选后还剩 ${filteredShops.length} 条数据`);
      return filteredShops;
    };
    
    // 应用筛选
    const applyFilter = () => {
      if (isUnmounted.value) return;
      
      // 重置页码到第一页
      currentPage.value = 1;
      
      if (searchQuery.value.trim()) {
        // 如果有搜索关键词，通过API查询
        searchShops();
      } else if (originalShopsCache.value.length > 0) {
        // 没有搜索关键词但有缓存数据，在前端筛选
        console.log('使用前端筛选，基于原始缓存数据');
        
        // 复制原始数据，然后应用筛选
        allShopsCache.value = [...originalShopsCache.value];
        
        // 如果有筛选条件，则应用筛选
        if (selectedRating.value || selectedPrice.value || selectedAverageCost.value || sortBy.value !== 'default') {
          // 过滤缓存数据
          const filteredData = filterCachedShops();
          // 更新数据
          allShopsCache.value = filteredData;
        }
        
        total.value = allShopsCache.value.length;
        // 更新当前页显示的数据
        updatePagedShops();
      } else {
        console.warn('既没有搜索关键词也没有缓存数据，无法应用筛选');
      }
    };
    
    // 查看商家详情
    const viewShopDetails = (shopId) => {
      if (isUnmounted.value) return;
      
      try {
        if (!shopId) {
          console.warn('无效的商家ID');
          return;
        }
        router.push({ name: 'ShopDetail', params: { id: shopId } });
      } catch (error) {
        console.error('导航到商家详情页失败:', error);
      }
    };
    
    // 重置筛选条件
    const resetFilters = () => {
      if (isUnmounted.value) return;
      
      selectedRating.value = '';
      selectedPrice.value = '';
      selectedAverageCost.value = '';
      sortBy.value = 'default';
      currentPage.value = 1;
      
      // 恢复原始数据
      if (originalShopsCache.value.length > 0) {
        console.log('重置筛选条件，恢复原始数据');
        allShopsCache.value = [...originalShopsCache.value];
        total.value = allShopsCache.value.length;
        updatePagedShops();
      } else {
        searchShops();
      }
    };
    
    // 重置筛选条件并重新搜索
    const resetAndSearch = () => {
      if (isUnmounted.value) return;
      
      errorMessage.value = '';
      searchQuery.value = '';
      selectedRating.value = '';
      selectedPrice.value = '';
      selectedAverageCost.value = '';
      sortBy.value = 'default';
      currentPage.value = 1;
      loadShops();
    };
    
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
        '奶茶': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
        '烧烤': 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
        '西餐': 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
        '中餐': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
        '快餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
        '甜品': 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg',
        '小吃': 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg',
        // 默认图片
        'default': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
      };
      
      return categoryImages[categoryName] || categoryImages.default;
    };
    
    // 格式化价格区间显示
    const formatPriceRange = (range) => {
      if (!range) return '';
      
      const [min, max] = range.split('-');
      if (max === '+') {
        return `￥${min}以上`;
      }
      return `￥${min}-${max}`;
    };
    
    // 处理每页显示条数变化
    const handlePageSizeChange = () => {
      if (isUnmounted.value) return;
      
      currentPage.value = 1;
      
      if (searchQuery.value.trim()) {
        // 有搜索关键词时，重新搜索
        searchShops();
      } else if (allShopsCache.value.length > 0) {
        // 没有搜索关键词且有缓存数据时，直接使用缓存数据分页
        updatePagedShops();
      }
    };
    
    return {
      Search,
      searchQuery,
      shops,
      loading,
      errorMessage,
      searchHistory,
      showSearchHistory,
      selectedRating,
      selectedPrice,
      selectedAverageCost,
      sortBy,
      searchShops,
      useHistoryItem,
      clearSearchHistory,
      applyFilter,
      viewShopDetails,
      resetFilters,
      resetAndSearch,
      getShopImage,
      formatPriceRange,
      defaultImage,
      currentPage,
      pageSize,
      total,
      totalPages,
      isUnmounted,
      handlePageChange,
      handlePageSizeChange
    };
  }
};
</script>

<style scoped>
body {
  overflow-x: hidden;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.shop-search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  min-height: 100vh;
}

.search-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 15px 0;
  margin-bottom: 15px;
}

.search-input {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 25px;
  padding: 0 20px;
}

.search-input :deep(.el-input__inner) {
  height: 45px;
  font-size: 16px;
}

.search-history {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 800px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-top: 10px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.history-header span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-item {
  padding: 6px 12px;
  background-color: #f5f7fa;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.3s ease;
}

.history-item:hover {
  background-color: #ecf5ff;
  color: #409EFF;
}

.filter-panel {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.filter-section {
  margin-bottom: 0;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-options :deep(.el-radio) {
  margin-right: 0;
}

.filter-options :deep(.el-radio__label) {
  padding: 6px 12px;
  border-radius: 16px;
  background-color: #f5f7fa;
  transition: all 0.3s ease;
}

.filter-options :deep(.el-radio__input.is-checked + .el-radio__label) {
  background-color: #ecf5ff;
  color: #409EFF;
}

.filter-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.filter-actions-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filter-actions-left .filter-title {
  margin-bottom: 0;
}

.filter-actions-left :deep(.el-select) {
  width: 120px;
}

.shop-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.shop-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.shop-avatar {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.shop-card:hover .shop-avatar img {
  transform: scale(1.05);
}

.shop-info {
  padding: 15px;
}

.shop-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.shop-rating span {
  margin-left: 8px;
  color: #ff9900;
  font-weight: 500;
}

.shop-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.shop-meta :deep(.el-tag) {
  border-radius: 12px;
  padding: 4px 10px;
}

.shop-address, .shop-hours {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.shop-address i, .shop-hours i {
  margin-right: 6px;
  font-size: 14px;
}

.shop-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-top: 8px;
}

.search-summary {
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.result-count {
  font-size: 14px;
  color: #606266;
}

.result-count strong {
  color: #409EFF;
  font-weight: 600;
}

.active-filters {
  display: flex;
  gap: 8px;
}

.active-filters span {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  padding: 20px 0;
}

.pagination-info {
  margin-left: 15px;
  font-size: 13px;
  color: #606266;
}

.loading, .no-results {
  background-color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.no-results h3 {
  color: #303133;
  font-size: 18px;
  margin-bottom: 10px;
}

.no-results p {
  color: #909399;
  font-size: 14px;
}

.error-message {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.retry-button {
  margin-top: 15px;
}

@media screen and (max-width: 768px) {
  .shop-search-container {
    padding: 10px;
  }
  
  .search-bar {
    padding: 10px 0;
  }
  
  .filter-panel {
    padding: 15px;
  }
  
  .shop-list {
    grid-template-columns: 1fr;
  }
  
  .shop-avatar {
    height: 160px;
  }
}
</style> 