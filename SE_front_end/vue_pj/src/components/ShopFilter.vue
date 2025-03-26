<template>
  <div class="shop-filter">
    <el-card class="filter-card">
      <!-- 评分筛选 -->
      <div class="filter-section">
        <div class="filter-title">评分</div>
        <div class="filter-content">
          <el-checkbox-group v-model="selectedRatings">
            <el-checkbox label="4.5">4.5分以上</el-checkbox>
            <el-checkbox label="4.0">4.0分以上</el-checkbox>
            <el-checkbox label="3.5">3.5分以上</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>

      <!-- 价格区间筛选 -->
      <div class="filter-section">
        <div class="filter-title">价格区间</div>
        <div class="filter-content">
          <el-radio-group v-model="selectedPriceRange">
            <el-radio label="0-50">￥0-50</el-radio>
            <el-radio label="50-100">￥50-100</el-radio>
            <el-radio label="100-200">￥100-200</el-radio>
            <el-radio label="200+">￥200以上</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 人均消费筛选 -->
      <div class="filter-section">
        <div class="filter-title">人均消费</div>
        <div class="filter-content">
          <el-slider
            v-model="averagePrice"
            range
            :min="0"
            :max="500"
            :step="10"
            :marks="{
              0: '￥0',
              100: '￥100',
              200: '￥200',
              300: '￥300',
              400: '￥400',
              500: '￥500'
            }"
          />
        </div>
      </div>

      <!-- 其他筛选条件 -->
      <div class="filter-section">
        <div class="filter-title">其他</div>
        <div class="filter-content">
          <el-checkbox-group v-model="selectedFeatures">
            <el-checkbox label="delivery">提供外卖</el-checkbox>
            <el-checkbox label="takeout">支持自提</el-checkbox>
            <el-checkbox label="parking">提供停车</el-checkbox>
            <el-checkbox label="wifi">免费WiFi</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>

      <!-- 筛选按钮 -->
      <div class="filter-actions">
        <el-button type="primary" @click="applyFilters">应用筛选</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'ShopFilter',
  setup(props, { emit }) {
    const selectedRatings = ref([])
    const selectedPriceRange = ref('')
    const averagePrice = ref([0, 200])
    const selectedFeatures = ref([])

    const applyFilters = () => {
      emit('filter', {
        ratings: selectedRatings.value,
        priceRange: selectedPriceRange.value,
        averagePrice: averagePrice.value,
        features: selectedFeatures.value
      })
    }

    const resetFilters = () => {
      selectedRatings.value = []
      selectedPriceRange.value = ''
      averagePrice.value = [0, 200]
      selectedFeatures.value = []
      applyFilters()
    }

    // 监听筛选条件变化，自动应用筛选
    watch([selectedRatings, selectedPriceRange, averagePrice, selectedFeatures], () => {
      applyFilters()
    }, { deep: true })

    return {
      selectedRatings,
      selectedPriceRange,
      averagePrice,
      selectedFeatures,
      applyFilters,
      resetFilters
    }
  }
}
</script>

<style scoped>
.shop-filter {
  width: 100%;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-section {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.filter-section:last-child {
  border-bottom: none;
}

.filter-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.filter-content {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 