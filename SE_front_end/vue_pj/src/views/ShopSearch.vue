<template>
  <div class="shop-search-container">
    <div class="search-bar">
      <div class="search-input">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索商家名称、分类等" 
          @keyup.enter="searchShops"
          @focus="handleSearchFocus"
          @input="handleSearchInput"
          @clear="handleClear"
          clearable>
          <template #suffix>
            <el-button :icon="Search" circle @click="searchShops"></el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 搜索历史记录 -->
      <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history" :class="{'search-history-collapsed': !historyExpanded}">
        <div class="history-header">
          <span>搜索历史</span>
          <div class="history-actions">
            <el-button type="text" @click="toggleHistoryExpand">
              <el-icon><component :is="historyExpanded ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              {{ historyExpanded ? '收起' : '展开' }}
            </el-button>
            <el-button type="text" @click="clearSearchHistory">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </div>
        </div>
        <div class="history-list" v-show="historyExpanded">
          <el-tag
            v-for="item in searchHistory"
            :key="item.id"
            class="history-item"
            @click="handleHistoryClick(item.keyword)"
          >
            <el-icon><Clock /></el-icon>
            {{ item.keyword }}
          </el-tag>
        </div>
      </div>
      
      <!-- 相似关键词提示 -->
      <div v-if="showSimilarKeywords && similarKeywords.length > 0" class="similar-keywords">
        <div class="similar-header">
          <span>您是否想搜：</span>
        </div>
        <div class="similar-list">
          <el-tag
            v-for="keyword in similarKeywords"
            :key="keyword"
            class="similar-item"
            @click="useSimilarKeyword(keyword)"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>
      
      <!-- 加载中状态 -->
      <div v-if="isLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
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
            <el-radio value="">全部</el-radio>
            <el-radio value="4.5">4.5分以上</el-radio>
            <el-radio value="4.0">4.0分以上</el-radio>
            <el-radio value="3.5">3.5分以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">价格区间</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedPrice" @change="applyFilter">
            <el-radio value="">全部</el-radio>
            <el-radio value="0-50">￥0-50</el-radio>
            <el-radio value="50-100">￥50-100</el-radio>
            <el-radio value="100-200">￥100-200</el-radio>
            <el-radio value="200+">￥200以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">人均消费</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedAverageCost" @change="applyFilter">
            <el-radio value="">全部</el-radio>
            <el-radio value="0-50">￥0-50/人</el-radio>
            <el-radio value="50-100">￥50-100/人</el-radio>
            <el-radio value="100-200">￥100-200/人</el-radio>
            <el-radio value="200+">￥200以上/人</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">排序方式</div>
        <div class="filter-options">
          <el-radio-group v-model="sortBy" @change="applyFilter">
            <el-radio value="default">综合排序</el-radio>
            <el-radio value="rating_desc">评分最高</el-radio>
            <el-radio value="average_cost_asc">人均消费最低</el-radio>
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
        <div>
          <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
          <el-button type="success" @click="debugFilters" v-if="userId === 1">调试筛选</el-button>
        </div>
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
          v-for="shopData in shops" 
          :key="shopData.shop ? shopData.shop.id : shopData.id" 
          class="shop-card" 
          @click="viewShopDetails(shopData)">
          <div class="shop-avatar">
            <img :src="getShopImage(shopData)" alt="店铺图片" />
          </div>
          <div class="shop-info">
            <h3 class="shop-name">{{ (shopData.shop ? shopData.shop.name : shopData.name) || '未命名商家' }}</h3>
            <div class="shop-rating">
              <el-rate 
                :model-value="(shopData.shop ? shopData.shop.rating : shopData.rating) || 0" 
                disabled 
                text-color="#ff9900" />
              <span>{{ (shopData.shop ? shopData.shop.rating : shopData.rating) || '暂无评分' }}</span>
            </div>
            <div class="shop-meta">
              <el-tag size="small" type="success" v-if="shopData.shop && shopData.shop.categoryName">{{ shopData.shop.categoryName }}</el-tag>
              <el-tag size="small" type="success" v-else-if="shopData.categoryName">{{ shopData.categoryName }}</el-tag>
              <el-tag size="small" type="warning">人均￥{{ (shopData.shop ? shopData.shop.averageCost : shopData.averageCost) || '未知' }}</el-tag>
              <el-tag size="small" type="info">￥{{ (shopData.shop ? shopData.shop.priceMin : shopData.priceMin) || '0' }}-￥{{ (shopData.shop ? shopData.shop.priceMax : shopData.priceMax) || '0' }}</el-tag>
            </div>
            <div class="shop-address"><i class="el-icon-location"></i> {{ (shopData.shop ? shopData.shop.address : shopData.address) || '暂无地址信息' }}</div>
            <div class="shop-hours"><i class="el-icon-time"></i> {{ (shopData.shop ? shopData.shop.businessHours : shopData.businessHours) || '暂无营业时间信息' }}</div>
            <div class="shop-description" v-if="shopData.shop && shopData.shop.description">{{ shopData.shop.description }}</div>
            <div class="shop-description" v-else-if="shopData.description">{{ shopData.description }}</div>
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
import { Search, Delete, Clock, Loading, ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import ShopService from '@/services/ShopService';
import AuthService from '@/services/AuthService';
import { useStore } from 'vuex';

export default {
  name: 'ShopSearch',
  
  setup() {
    const router = useRouter();
    const store = useStore();
    
    // 搜索相关
    const searchQuery = ref('');
    const shops = ref([]);
    const loading = ref(false);
    const showSearchHistory = ref(false);
    const searchHistory = ref([]);
    const isLoading = ref(false);
    const historyExpanded = ref(true); // 默认展开搜索历史
    const defaultImage = 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png';
    
    // 相似关键词相关
    const showSimilarKeywords = ref(false);
    const similarKeywords = ref([]);
    
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
    
    // 用户信息 - 使用计算属性从Vuex中获取
    const user = computed(() => store.getters['auth/currentUser']);
    const userId = computed(() => {
      const currentUser = user.value;
      // 调试用户信息获取
      console.log('获取当前用户信息:', currentUser);
      if (!currentUser) {
        console.warn('用户未登录或用户信息不完整');
        return null;
      }
      
      // 如果用户对象没有id属性，尝试获取完整的用户信息
      if (!currentUser.id) {
        console.warn('用户对象中缺少id字段，尝试从AuthService获取');
        // 使用AuthService获取完整用户信息
        const fullUserInfo = AuthService.getUser();
        if (fullUserInfo && fullUserInfo.id) {
          console.log('从AuthService获取到用户ID:', fullUserInfo.id);
          return fullUserInfo.id;
        }
        // 在紧急情况下使用硬编码ID (注意：这是临时解决方案，应当尽快修复)
        console.warn('无法获取用户ID，使用临时ID (1) 作为应急措施');
        return 1;
      }
      
      // 确保userId是数字类型
      const idValue = Number(currentUser.id);
      console.log('从用户对象获取用户ID:', idValue, '类型:', typeof idValue);
      
      return idValue;
    });
    
    // 在setup中添加错误消息状态
    const errorMessage = ref('');
    
    // 监听搜索框内容变化
    watch(searchQuery, (newValue) => {
      console.log('搜索框内容变化:', newValue);
      if (!newValue || newValue.trim() === '') {
        if (userId.value) {
          console.log('搜索框为空，显示搜索历史，用户ID:', userId.value);
          showSearchHistory.value = true;
          loadSearchHistory();
        }
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
        console.log('开始加载商家数据...');
        
        // 如果已有全部商家数据缓存，直接使用缓存数据
        if (originalShopsCache.value.length > 0) {
          console.log('使用缓存的商家数据，共', originalShopsCache.value.length, '条');
          allShopsCache.value = [...originalShopsCache.value];
          total.value = allShopsCache.value.length;
          updatePagedShops();
          loading.value = false;
          return;
        }
        
        // 获取较大数量的商家数据
        console.log('从服务器请求商家数据');
        const response = await ShopService.getShops(1, 100);
        console.log('服务器响应数据:', JSON.stringify(response, null, 2));
        
        if (isUnmounted.value) return;
        
        if (response.code === 1 && response.data) {
          console.log('响应数据结构:', {
            isArray: Array.isArray(response.data),
            hasRecords: response.data.records !== undefined,
            dataType: typeof response.data
          });
          
          // 处理不同类型的响应数据结构
          if (Array.isArray(response.data)) {
            originalShopsCache.value = response.data;
            allShopsCache.value = [...originalShopsCache.value];
            console.log('数组结构 - 商家数据:', JSON.stringify(allShopsCache.value[0], null, 2));
          } else if (response.data.records) {
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
      try {
        // 使用计算属性获取用户ID
        const currentUserId = userId.value;
        console.log('加载搜索历史 - 当前用户ID:', currentUserId);
        
        if (!currentUserId) {
          console.log('用户未登录或无法获取用户ID，无法加载搜索历史');
          showSearchHistory.value = false;
          return;
        }

        isLoading.value = true;
        console.log('开始加载搜索历史，用户ID:', currentUserId);
        
        const response = await ShopService.getSearchHistory(currentUserId);
        console.log('搜索历史响应:', response);

        if (response.code === 1 && Array.isArray(response.data)) {
          searchHistory.value = response.data
            .sort((a, b) => new Date(b.searchTime) - new Date(a.searchTime))
            .slice(0, 10);
          showSearchHistory.value = searchHistory.value.length > 0;
          console.log('搜索历史加载成功:', searchHistory.value);
        } else {
          console.warn('加载搜索历史失败:', response.msg);
          searchHistory.value = [];
          showSearchHistory.value = false;
        }
      } catch (error) {
        console.error('加载搜索历史出错:', error);
        searchHistory.value = [];
        showSearchHistory.value = false;
      } finally {
        isLoading.value = false;
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
        // 基础搜索参数 - 确保每次创建新对象，避免参数残留
        const queryParams = {
          name: searchQuery.value,
          pageSize: 100, // 获取更多数据，在前端分页
          pageCurrent: 1, // 从第一页开始获取
          sortBy: sortBy.value
        };
        
        console.log('创建新的查询参数对象:', queryParams);
        
        // 只有在用户已登录时才添加userId
        if (userId.value) {
          // 确保userId是数值类型
          const userIdValue = userId.value;
          console.log('准备添加用户ID参数:', userIdValue, '类型:', typeof userIdValue);
          
          // 尝试将userId转换为数值类型
          try {
            queryParams.userId = Number(userIdValue);
            console.log('转换后的用户ID参数:', queryParams.userId, '类型:', typeof queryParams.userId);
          } catch (e) {
            // 如果转换失败，使用原始值
            console.warn('用户ID转换为数值失败，使用原值:', userIdValue);
            queryParams.userId = userIdValue;
          }
        } else {
          console.warn('未找到有效的用户ID，搜索历史将不会被记录');
        }
        
        // 添加评分筛选
        if (selectedRating.value) {
          queryParams.minRating = Number(selectedRating.value);
        }
        
        // 添加价格筛选
        if (selectedPrice.value) {
          const [min, max] = selectedPrice.value.split('-');
          const minPrice = Number(min);
          
          if (max && max.includes('+')) {
            // 处理"200+"这种格式（表示200以上）
            console.log('价格筛选 - 200以上，使用最高价>=200筛选');
            // 对于200+，我们应该筛选最高价>=200的商家，使用maxPriceMin参数
            queryParams.maxPriceMin = minPrice; // 商家最高价>=200
            // 清除可能冲突的参数
            delete queryParams.minPrice;
            delete queryParams.maxPrice;
          } else {
            // 普通范围筛选
            if (!isNaN(minPrice)) {
              queryParams.minPrice = minPrice;
              console.log('价格筛选 - 最低价 >=', minPrice);
            }
            
            if (max) {
              const maxPrice = Number(max);
              if (!isNaN(maxPrice)) {
                queryParams.maxPrice = maxPrice;
                console.log('价格筛选 - 最高价 <=', maxPrice);
              }
            }
            // 清除可能的自定义参数
            delete queryParams.maxPriceMin;
          }
        } else {
          // 清除以前可能设置的价格筛选参数
          delete queryParams.minPrice;
          delete queryParams.maxPrice;
          delete queryParams.maxPriceMin;
        }
        
        // 添加人均消费筛选 - 人均消费是单一值，不需要交集逻辑
        if (selectedAverageCost.value) {
          const [min, max] = selectedAverageCost.value.split('-');
          const minCost = Number(min);
          
          if (max && max.includes('+')) {
            // 处理"200+"这种格式（表示200以上的人均消费）
            console.log('人均消费筛选 - 设置下限为:', minCost);
            
            // 设置最低人均消费
            queryParams.minAverageCost = minCost;
            delete queryParams.maxAverageCost;
            
            console.log('人均消费"200+"筛选参数:', { minAverageCost: queryParams.minAverageCost });
          } else {
            // 普通范围筛选
            if (!isNaN(minCost)) {
              queryParams.minAverageCost = minCost;
              console.log('人均消费筛选 - 下限 >=', minCost);
            }
            
            if (max) {
              const maxCost = Number(max);
              if (!isNaN(maxCost)) {
                queryParams.maxAverageCost = maxCost;
                console.log('人均消费筛选 - 上限 <=', maxCost);
              }
            }
          }
        } else {
          // 清除以前可能设置的人均消费筛选参数
          delete queryParams.minAverageCost;
          delete queryParams.maxAverageCost;
        }
        
        // 最终记录要发送的请求参数
        console.log('=======================================');
        console.log('最终发送的搜索参数:', JSON.stringify(queryParams, null, 2));
        console.log('=======================================');
        
        console.log('发送搜索请求...');
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
      
      const currentUserId = userId.value;
      if (!currentUserId) {
        console.warn('清空搜索历史失败: 无法获取用户ID');
        return;
      }
      
      try {
        console.log('清空搜索历史，用户ID:', currentUserId);
        // 调用后端API清空搜索历史
        const response = await ShopService.clearSearchHistory(currentUserId);
        if (response.code === 1) {
          searchHistory.value = [];
          showSearchHistory.value = false;
          console.log('搜索历史清空成功');
        } else {
          console.warn('清空搜索历史失败:', response.msg);
        }
      } catch (error) {
        console.error('清空搜索历史出错:', error);
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
            // 处理两种可能的数据结构
            const shopObj = shop.shop || shop;
            const rating = Number(shopObj.rating);
            console.log(`商家 ${shopObj.name || '未知'} 评分: ${rating}, 筛选条件: >= ${minRating}`);
            return !isNaN(rating) && rating >= minRating;
          });
        }
      }
      
      // 应用价格筛选
      if (selectedPrice.value) {
        const [min, max] = selectedPrice.value.split('-');
        const minPrice = Number(min);
        
        if (!isNaN(minPrice)) {
          // 检查是否是"200+"这种格式（表示200以上）
          if (max && max.includes('+')) {
            console.log('应用价格筛选 >=', minPrice, '（无上限）');
            filteredShops = filteredShops.filter(shop => {
              // 处理两种可能的数据结构
              const shopObj = shop.shop || shop;
              const shopMaxPrice = Number(shopObj.priceMax) || 0;
              // 对于"200+"的筛选，商家的最高价格需要大于等于200
              console.log(`商家 ${shopObj.name || '未知'} 价格区间: ${shopObj.priceMin}-${shopMaxPrice}, 筛选条件: 最高价 >= ${minPrice}`);
              // 商家最高价格大于等于筛选价格
              return !isNaN(shopMaxPrice) && shopMaxPrice >= minPrice;
            });
          } else {
            // 普通范围筛选（如0-50、50-100等）- 只要有交集即可
            const maxPrice = max ? Number(max) : Infinity;
            console.log('应用价格区间筛选, 筛选区间:', minPrice, '-', maxPrice);
            
            filteredShops = filteredShops.filter(shop => {
              // 处理两种可能的数据结构
              const shopObj = shop.shop || shop;
              const shopMinPrice = Number(shopObj.priceMin) || 0;
              const shopMaxPrice = Number(shopObj.priceMax) || 0;
              
              // 判断两个区间是否有交集
              // 商家最低价 <= 筛选最高价 且 商家最高价 >= 筛选最低价
              const hasIntersection = shopMinPrice <= maxPrice && shopMaxPrice >= minPrice;
              
              console.log(`商家 ${shopObj.name || '未知'} 价格区间: ${shopMinPrice}-${shopMaxPrice}, 筛选区间: ${minPrice}-${maxPrice}, 有交集: ${hasIntersection}`);
              
              return hasIntersection;
            });
          }
        }
      }
      
      // 应用人均消费筛选 - 同样修改为区间交集筛选
      if (selectedAverageCost.value) {
        const [min, max] = selectedAverageCost.value.split('-');
        const minCost = Number(min);
        
        if (!isNaN(minCost)) {
          // 检查是否是"200+"这种格式（表示200以上）
          if (max && max.includes('+')) {
            console.log('应用人均消费筛选 >=', minCost, '（无上限）');
            filteredShops = filteredShops.filter(shop => {
              // 处理两种可能的数据结构
              const shopObj = shop.shop || shop;
              const averageCost = Number(shopObj.averageCost);
              console.log(`商家 ${shopObj.name || '未知'} 人均消费: ${averageCost}, 筛选条件: >= ${minCost}`);
              // 人均消费大于等于筛选最低值
              const result = !isNaN(averageCost) && averageCost >= minCost;
              console.log(`筛选结果: ${result ? '通过' : '不通过'}`);
              return result;
            });
          } else {
            // 普通范围筛选 - 对于人均消费，直接判断是否在范围内
            const maxCost = max ? Number(max) : Infinity;
            console.log('应用人均消费筛选, 区间:', minCost, '-', maxCost);
            
            filteredShops = filteredShops.filter(shop => {
              // 处理两种可能的数据结构
              const shopObj = shop.shop || shop;
              const averageCost = Number(shopObj.averageCost) || 0;
              
              // 人均消费在区间内
              const isInRange = averageCost >= minCost && (max.includes('+') || averageCost <= maxCost);
              
              console.log(`商家 ${shopObj.name || '未知'} 人均消费: ${averageCost}, 筛选区间: ${minCost}-${maxCost}, 在范围内: ${isInRange}`);
              
              return isInRange;
            });
          }
        }
      }
      
      // 应用排序
      if (sortBy.value !== 'default') {
        if (sortBy.value === 'rating_desc') {
          console.log('应用评分降序排序');
          filteredShops.sort((a, b) => {
            const shopA = a.shop || a;
            const shopB = b.shop || b;
            const ratingA = Number(shopA.rating) || 0;
            const ratingB = Number(shopB.rating) || 0;
            return ratingB - ratingA;
          });
        } else if (sortBy.value === 'average_cost_asc') {
          console.log('应用人均消费升序排序');
          filteredShops.sort((a, b) => {
            const shopA = a.shop || a;
            const shopB = b.shop || b;
            const costA = Number(shopA.averageCost) || 0;
            const costB = Number(shopB.averageCost) || 0;
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
    const viewShopDetails = (shopData) => {
      if (isUnmounted.value) return;
      
      try {
        if (!shopData) {
          console.warn('无效的商家数据');
          return;
        }
        
        let shopId;
        // 处理两种可能的数据结构
        if (shopData.shop && shopData.shop.id) {
          // 第一种数据结构：{shop: {...}, images: [...]}
          shopId = shopData.shop.id;
          console.log('从嵌套结构中获取商家ID:', shopId);
        } else if (shopData.id) {
          // 第二种数据结构：直接是商家对象
          shopId = shopData.id;
          console.log('直接从商家对象获取ID:', shopId);
        } else {
          console.error('无法从数据中提取商家ID:', shopData);
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
    
    // 获取商家图片
    const getShopImage = (shopData) => {
      try {
        console.log('处理商家图片，数据:', JSON.stringify(shopData, null, 2));
        
        if (!shopData) {
          console.error('shopData为空');
          return defaultImage;
        }
        
        // 获取正确的shop对象和图片数组
        let shop, images;
        
        // 检查数据结构 - 有两种可能的情况
        if (shopData.shop && shopData.images) {
          // 第一种数据结构：{shop: {...}, images: [...]}
          // 这是后端page和search接口新的统一结构
          shop = shopData.shop;
          images = shopData.images;
          console.log('接口返回shop和images结构:', shop.id);
        } else if (shopData.id) {
          // 第二种数据结构：直接是商家对象
          // 这可能是旧版API或者前端缓存数据
          shop = shopData;
          images = [];
          console.log('接口只返回shop对象:', shop.id);
        } else {
          console.error('无法解析商家数据结构:', Object.keys(shopData));
          return defaultImage;
        }
        
        // 处理图片数据
        if (Array.isArray(images) && images.length > 0) {
          console.log(`商家[${shop.id}]有${images.length}张图片`);
          const imageData = images[0]; // 使用第一张图片
          
          if (imageData && imageData.imageUrl) {
            let imageUrl = imageData.imageUrl;
            console.log('原始图片URL:', imageUrl);
            
            // 统一处理图片URL
            if (imageUrl.startsWith('http')) {
              // 如果是完整的URL，直接使用
              console.log('使用完整URL:', imageUrl);
              return imageUrl;
            } else {
              // 如果是相对路径，添加后端服务器地址
              // 移除开头的斜杠（如果有）
              imageUrl = imageUrl.replace(/^\/+/, '');
              // 添加后端服务器地址
              imageUrl = `http://localhost:8088/${imageUrl}`;
              console.log('处理后的图片URL:', imageUrl);
              return imageUrl;
            }
          }
        } else {
          console.log(`商家[${shop.id}]没有图片数据`);
        }
        
        // 如果没有图片，返回基于分类的默认图片
        const categoryName = shop.categoryName || '未分类';
        console.log('使用默认图片，分类:', categoryName);
        
        const categoryImages = {
          '火锅': 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
          '奶茶': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
          '烧烤': 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
          '西餐': 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
          '中餐': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
          '快餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
          '甜品': 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg',
          '小吃': 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg',
          '海鲜': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
          '自助餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
          '未分类': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
        };
        
        return categoryImages[categoryName] || categoryImages['未分类'];
      } catch (error) {
        console.error('获取商家图片时发生错误:', error);
        return defaultImage;
      }
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
    
    // 添加调试筛选函数
    const debugFilters = () => {
      console.log('===== 调试筛选条件 =====');
      console.log('当前筛选条件:');
      console.log('- 评分:', selectedRating.value);
      console.log('- 价格区间:', selectedPrice.value);
      console.log('- 人均消费:', selectedAverageCost.value);
      console.log('- 排序方式:', sortBy.value);
      console.log('=======================');
      
      // 强制应用筛选
      applyFilter();
    };
    
    // 在 setup 函数中添加以下内容
    const handleSearchFocus = async () => {
      console.log('搜索框获得焦点');
      if (userId.value) {
        console.log('用户已登录，显示搜索历史，用户ID:', userId.value);
        showSearchHistory.value = true;
        await loadSearchHistory();
      }
    };
    
    // 修改清空搜索框的处理
    const handleClear = async () => {
      console.log('清空搜索框');
      searchQuery.value = '';
      if (userId.value) {
        console.log('清空后显示搜索历史，用户ID:', userId.value);
        showSearchHistory.value = true;
        await loadSearchHistory();
      }
    };
    
    // 修改搜索历史点击处理方法
    const handleHistoryClick = (keyword) => {
      console.log('点击搜索历史:', keyword, '用户ID:', userId.value);
      searchQuery.value = keyword;
      showSearchHistory.value = false;
      searchShops();
    };
    
    // 监听搜索框输入，获取相似关键词
    const handleSearchInput = async (value) => {
      if (value && value.trim().length >= 2) {
        // 获取相似关键词
        await getSimilarKeywords(value);
      } else {
        showSimilarKeywords.value = false;
        similarKeywords.value = [];
      }
    };

    // 获取相似关键词
    const getSimilarKeywords = async (keyword) => {
      try {
        if (!keyword || keyword.trim().length < 2) return;
        
        console.log('获取相似关键词，关键词:', keyword);
        const response = await ShopService.getSimilarKeywords(keyword.trim());
        
        if (response && response.code === 1 && response.data && response.data.length > 0) {
          console.log('获取到相似关键词:', response.data);
          similarKeywords.value = response.data;
          showSimilarKeywords.value = true;
        } else {
          console.log('没有找到相似关键词');
          similarKeywords.value = [];
          showSimilarKeywords.value = false;
        }
      } catch (error) {
        console.error('获取相似关键词出错:', error);
        similarKeywords.value = [];
        showSimilarKeywords.value = false;
      }
    };

    // 使用相似关键词进行搜索
    const useSimilarKeyword = (keyword) => {
      searchQuery.value = keyword;
      showSimilarKeywords.value = false;
      searchShops();
    };
    
    // 收起/展开搜索历史
    const toggleHistoryExpand = () => {
      historyExpanded.value = !historyExpanded.value;
    };
    
    return {
      Search,
      Delete,
      Clock,
      Loading,
      ArrowDown,
      ArrowUp,
      searchQuery,
      shops,
      loading,
      errorMessage,
      searchHistory,
      showSearchHistory,
      historyExpanded,
      isLoading,
      selectedRating,
      selectedPrice,
      selectedAverageCost,
      sortBy,
      searchShops,
      useHistoryItem,
      clearSearchHistory,
      toggleHistoryExpand,
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
      handlePageSizeChange,
      debugFilters,
      handleSearchFocus,
      handleClear,
      handleHistoryClick,
      // 相似关键词相关
      showSimilarKeywords,
      similarKeywords,
      handleSearchInput,
      getSimilarKeywords,
      useSimilarKeyword
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
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
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
  top: calc(100% + 5px);
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 800px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-top: 0;
  z-index: 101;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.search-history-collapsed {
  padding: 10px 15px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  max-height: 40px; /* 调整收起后的高度 */
  opacity: 0.5; /* 稍微降低透明度 */
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.search-history-collapsed .history-header {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.history-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.history-actions :deep(.el-button) {
  padding: 4px 8px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.history-actions :deep(.el-button):hover {
  background-color: #f0f2f500;
  border-radius: 4px;
}

.history-actions :deep(.el-icon) {
  margin-right: 4px;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  transition: all 0.3s ease-in-out;
  overflow: hidden;
  max-height: 300px; /* 设置展开时的最大高度 */
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

.similar-keywords {
  position: absolute;
  width: 100%;
  max-width: 800px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-top: 10px;
  z-index: 100;
}

.similar-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.similar-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.similar-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.similar-item:hover {
  background-color: #409EFF;
  color: white;
}

.history-header span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
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