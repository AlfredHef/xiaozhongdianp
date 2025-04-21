<!-- Order Confirmation Page -->
<template>
  <div class="order-confirm">
    <div class="navigation">
      <el-button icon="el-icon-arrow-left" @click="goBack">返回套餐详情</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="!packageDetail" class="no-data">
      <h2>未找到套餐信息</h2>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>

    <div v-else class="order-content">
      <h1>确认订单</h1>

      <div class="package-info">
        <h2>套餐信息</h2>
        <div class="package-card">
          <div class="package-title">{{ packageDetail.title }}</div>
          <div class="package-price">¥{{ packageDetail.price }}</div>
        </div>
      </div>

      <div class="coupon-section">
        <h2>选择优惠券</h2>
        <div v-if="loadingCoupons" class="loading-coupons">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="!coupons || coupons.length === 0" class="no-coupons">
          <p>暂无可用优惠券</p>
        </div>
        <div v-else class="coupon-selection">
          <el-radio-group v-model="selectedCouponId">
            <el-radio :label="null">不使用优惠券</el-radio>
            <el-radio 
              v-for="coupon in coupons" 
              :key="coupon.id" 
              :label="coupon.id" 
              class="coupon-radio-item"
            >
              <div class="coupon-info">
                <div class="coupon-amount">¥{{ coupon.discountAmount }}</div>
                <div class="coupon-details">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-validity">有效期至: {{ formatDate(coupon.expireDate) }}</div>
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <div class="order-summary">
        <div class="summary-row">
          <span>套餐原价：</span>
          <span>¥{{ packageDetail.price }}</span>
        </div>
        <div class="summary-row" v-if="selectedCoupon">
          <span>优惠券：</span>
          <span>-¥{{ selectedCoupon.discountAmount }}</span>
        </div>
        <div class="summary-row total">
          <span>实付金额：</span>
          <span>¥{{ finalPrice }}</span>
        </div>
      </div>

      <div class="submit-section">
        <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">确认下单</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import GroupBuyService from '@/services/GroupBuyService';

export default {
  name: 'OrderConfirm',
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    const packageDetail = ref(null);
    const coupons = ref([]);
    const selectedCouponId = ref(null);
    const loading = ref(true);
    const loadingCoupons = ref(true);
    const submitting = ref(false);

    // 加载套餐详情
    const loadPackageDetail = async () => {
      const packageId = route.params.id;
      if (!packageId) {
        loading.value = false;
        return;
      }

      try {
        loading.value = true;
        const response = await GroupBuyService.getPackageDetail(packageId);
        packageDetail.value = response;
      } catch (error) {
        console.error('获取套餐详情失败:', error);
        ElMessage.error('获取套餐详情失败');
      } finally {
        loading.value = false;
      }
    };

    // 加载用户可用优惠券
    const loadCoupons = async () => {
      try {
        loadingCoupons.value = true;
        const response = await GroupBuyService.getUserCoupons();
        coupons.value = response || [];
        
        // 默认选择减免金额最高的优惠券
        if (coupons.value.length > 0) {
          const bestCoupon = coupons.value.reduce((prev, current) => 
            (prev.discountAmount > current.discountAmount) ? prev : current
          );
          selectedCouponId.value = bestCoupon.id;
        }
      } catch (error) {
        console.error('获取优惠券失败:', error);
      } finally {
        loadingCoupons.value = false;
      }
    };

    // 返回上一页
    const goBack = () => {
      router.go(-1);
    };

    // 提交订单
    const submitOrder = async () => {
      if (!packageDetail.value || !packageDetail.value.id) {
        ElMessage.error('套餐信息不完整，无法下单');
        return;
      }

      try {
        submitting.value = true;
        const response = await GroupBuyService.createOrder(
          packageDetail.value.id, 
          selectedCouponId.value
        );
        
        // 下单成功，跳转到券码详情页
        if (response && response.orderId) {
          ElMessage.success('下单成功');
          router.push({ 
            name: 'VoucherDetail', 
            params: { id: response.orderId } 
          });
        } else {
          ElMessage.error('下单失败，请重试');
        }
      } catch (error) {
        console.error('下单失败:', error);
        ElMessage.error('下单失败，请重试');
      } finally {
        submitting.value = false;
      }
    };

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
    };

    // 计算属性：已选优惠券
    const selectedCoupon = computed(() => {
      if (!selectedCouponId.value) return null;
      return coupons.value.find(coupon => coupon.id === selectedCouponId.value);
    });

    // 计算属性：最终价格
    const finalPrice = computed(() => {
      if (!packageDetail.value) return 0;
      
      let price = packageDetail.value.price;
      if (selectedCoupon.value) {
        price = Math.max(0, price - selectedCoupon.value.discountAmount);
      }
      return price.toFixed(2);
    });

    onMounted(() => {
      loadPackageDetail();
      loadCoupons();
    });

    return {
      packageDetail,
      coupons,
      selectedCouponId,
      loading,
      loadingCoupons,
      submitting,
      goBack,
      submitOrder,
      formatDate,
      selectedCoupon,
      finalPrice
    };
  }
}
</script>

<style scoped>
.order-confirm {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.navigation {
  margin-bottom: 20px;
}

.loading, .no-data {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.order-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.order-content h1 {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  text-align: center;
}

.package-info, .coupon-section, .order-summary {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.package-info h2, .coupon-section h2 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.package-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.package-title {
  font-size: 16px;
  font-weight: bold;
}

.package-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.coupon-selection {
  margin-top: 20px;
}

.coupon-radio-item {
  display: block;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
}

.coupon-info {
  display: flex;
  align-items: center;
}

.coupon-amount {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 20px;
}

.coupon-details {
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 14px;
  font-weight: bold;
}

.coupon-validity {
  font-size: 12px;
  color: #999;
}

.loading-coupons, .no-coupons {
  padding: 20px;
  text-align: center;
  color: #999;
}

.order-summary {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border-bottom: none;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-row.total {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.submit-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-section button {
  width: 200px;
  padding: 12px 0;
  font-size: 16px;
}
</style> 