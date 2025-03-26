<template>
  <div class="shop-search-page">
    <!-- 用户信息栏 -->
    <div class="user-info-bar">
      <el-dropdown @command="handleCommand">
        <span class="user-dropdown">
          <el-avatar :size="32" :src="userAvatar">{{ userNameFirstChar }}</el-avatar>
          <span class="username">{{ username }}</span>
          <el-icon><CaretBottom /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-container>
      <el-aside width="300px">
        <!-- 筛选组件 -->
        <ShopFilter @filter="handleFilter" />
      </el-aside>
      
      <el-main>
        <!-- 搜索组件 -->
        <ShopSearch @search="handleSearch" />
        
        <!-- 商家列表组件 -->
        <ShopList
          :shops="filteredShops"
          :loading="loading"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ShopSearch from '@/components/ShopSearch.vue'
import ShopFilter from '@/components/ShopFilter.vue'
import ShopList from '@/components/ShopList.vue'
import { searchShops } from '@/api/shop'
import AuthService from '@/services/AuthService'

export default {
  name: 'ShopSearchPage',
  components: {
    ShopSearch,
    ShopFilter,
    ShopList,
    CaretBottom
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const shops = ref([])
    const searchKeyword = ref('')
    const username = ref('')
    const userAvatar = ref('')
    const total = ref(0)
    const sortType = ref('')
    const pageSize = ref(10)

    const userNameFirstChar = computed(() => {
      return username.value ? username.value.charAt(0).toUpperCase() : '?'
    })

    const filterConditions = ref({
      ratings: [],
      priceRange: '',
      averagePrice: [0, 500],
      features: []
    })

    // 获取用户信息
    const loadUserInfo = async () => {
      try {
        // 先尝试从本地存储获取用户信息
        const localUserInfo = AuthService.getLocalUserInfo()
        if (localUserInfo) {
          username.value = localUserInfo.username
          userAvatar.value = localUserInfo.avatar
        }
        
        // 然后从服务器获取最新的用户信息
        const userInfo = await AuthService.getCurrentUser()
        if (userInfo) {
          username.value = userInfo.username
          userAvatar.value = userInfo.avatar
          // 更新本地存储
          AuthService.updateLocalUserInfo({
            username: userInfo.username,
            avatar: userInfo.avatar
          })
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        ElMessage.warning('获取用户信息失败，请重新登录')
        router.push('/login')
      }
    }

    // 处理下拉菜单命令
    const handleCommand = (command) => {
      switch (command) {
        case 'logout':
          AuthService.logout()
          router.push('/login')
          ElMessage.success('已退出登录')
          break
        case 'profile':
          router.push('/profile')
          break
      }
    }

    // 根据搜索关键词和筛选条件过滤商家列表
    const filteredShops = computed(() => {
      let result = [...shops.value]

      // 关键词搜索
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(shop => 
          shop.name.toLowerCase().includes(keyword) ||
          shop.tags.some(tag => tag.toLowerCase().includes(keyword))
        )
      }

      // 评分筛选
      if (filterConditions.value.ratings.length > 0) {
        result = result.filter(shop =>
          filterConditions.value.ratings.some(rating => shop.rating >= parseFloat(rating))
        )
      }

      // 价格区间筛选
      if (filterConditions.value.priceRange) {
        const [min, max] = filterConditions.value.priceRange.split('-').map(Number)
        result = result.filter(shop => {
          if (max) {
            return shop.averagePrice >= min && shop.averagePrice <= max
          }
          return shop.averagePrice >= min
        })
      }

      // 人均消费筛选
      const [minPrice, maxPrice] = filterConditions.value.averagePrice
      result = result.filter(shop =>
        shop.averagePrice >= minPrice && shop.averagePrice <= maxPrice
      )

      // 特色服务筛选
      if (filterConditions.value.features.length > 0) {
        result = result.filter(shop =>
          filterConditions.value.features.every(feature =>
            shop.services.includes(feature)
          )
        )
      }

      return result
    })

    // 处理搜索
    const handleSearch = async (keyword) => {
      try {
        loading.value = true
        searchKeyword.value = keyword
        const response = await searchShops({
          keyword: keyword,
          page: 1,
          size: pageSize.value,
          sortType: sortType.value,
          ...filterConditions.value
        })
        shops.value = response.records || []
        total.value = response.total || 0
      } catch (error) {
        console.error('搜索商家失败:', error)
        ElMessage.error('搜索失败，请重试')
      } finally {
        loading.value = false
      }
    }

    // 处理筛选
    const handleFilter = async (conditions) => {
      filterConditions.value = conditions
      await handleSearch(searchKeyword.value)
    }

    // 处理分页
    const handlePageChange = async (page) => {
      try {
        loading.value = true
        const response = await searchShops({
          keyword: searchKeyword.value,
          page: page,
          size: pageSize.value,
          sortType: sortType.value,
          ...filterConditions.value
        })
        shops.value = response.records || []
        total.value = response.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载失败，请重试')
      } finally {
        loading.value = false
      }
    }

    // 处理排序
    const handleSortChange = async (type) => {
      sortType.value = type
      await handleSearch(searchKeyword.value)
    }

    onMounted(() => {
      loadUserInfo()
    })

    return {
      loading,
      filteredShops,
      handleSearch,
      handleFilter,
      username,
      userAvatar,
      userNameFirstChar,
      handleCommand,
      handlePageChange,
      handleSortChange,
      total,
      pageSize
    }
  }
}
</script>

<style scoped>
.shop-search-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
}

.user-info-bar {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.el-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  padding: 20px;
  height: calc(100vh - 40px);
  position: sticky;
  top: 20px;
}

.el-main {
  padding: 20px;
  padding-top: 80px; /* 为用户信息栏留出更多空间 */
}
</style> 