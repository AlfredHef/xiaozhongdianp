<template>
  <div class="shop-search-page">
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
        />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import ShopSearch from '@/components/ShopSearch.vue'
import ShopFilter from '@/components/ShopFilter.vue'
import ShopList from '@/components/ShopList.vue'

export default {
  name: 'ShopSearchPage',
  components: {
    ShopSearch,
    ShopFilter,
    ShopList
  },
  setup() {
    const loading = ref(false)
    const shops = ref([])
    const searchKeyword = ref('')
    const filterConditions = ref({
      ratings: [],
      priceRange: '',
      averagePrice: [0, 500],
      features: []
    })

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
        // TODO: 调用后端API获取搜索结果
        // const response = await api.searchShops(keyword)
        // shops.value = response.data

        // 模拟数据
        setTimeout(() => {
          shops.value = [
            {
              id: 1,
              name: '川味火锅店',
              rating: 4.5,
              reviewCount: 1000,
              tags: ['火锅', '川菜'],
              imageUrl: 'https://example.com/image1.jpg',
              address: '北京市朝阳区xxx街xxx号',
              averagePrice: 88,
              services: ['提供WiFi', '免费停车']
            },
            // ... 更多模拟数据
          ]
          loading.value = false
        }, 1000)
      } catch (error) {
        console.error('搜索商家失败:', error)
        loading.value = false
      }
    }

    // 处理筛选
    const handleFilter = (conditions) => {
      filterConditions.value = conditions
    }

    return {
      loading,
      filteredShops,
      handleSearch,
      handleFilter
    }
  }
}
</script>

<style scoped>
.shop-search-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  padding: 20px;
}

.el-main {
  padding: 20px;
}
</style> 