<template>
  <div class="my-coupons">
    <h2>我的券包</h2>
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="coupons.length === 0" class="no-coupons">
      <p>暂无优惠券</p>
    </div>
    <div v-else class="coupon-list">
      <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card" :class="getCouponStatusClass(coupon)">
        <div class="coupon-left">
          <div class="coupon-amount">
            <span class="currency">¥</span>
            <span class="amount">{{ coupon.amount }}</span>
          </div>
          <div class="coupon-type">{{ getCouponTypeText(coupon.type) }}</div>
        </div>
        <div class="coupon-right">
          <div class="coupon-title">{{ coupon.title }}</div>
          <div class="coupon-desc">{{ coupon.description }}</div>
          <div class="coupon-conditions">
            <div v-if="coupon.useThreshold">满{{ coupon.useThreshold }}元可用</div>
            <div v-if="coupon.applicableCategory">适用品类：{{ coupon.applicableCategory }}</div>
            <div v-if="coupon.applicableShop">适用店铺：{{ coupon.applicableShop }}</div>
          </div>
          <div class="coupon-validity">
            <div v-if="coupon.expirationDate">有效期至：{{ formatDate(coupon.expirationDate) }}</div>
            <div v-else-if="coupon.validDays">领取后{{ coupon.validDays }}天内有效</div>
          </div>
          <div class="coupon-status">{{ getStatusText(coupon.status) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { formatDate } from '@/utils/date';
import { ElMessage } from 'element-plus';
import { mapState } from 'vuex';

export default {
  name: 'MyCoupons',
  computed: {
    ...mapState({
      vuexUser: state => state.auth && state.auth.user
    })
  },
  data() {
    return {
      coupons: [],
      loading: true
    };
  },
  created() {
    this.fetchCoupons();
  },
  methods: {
    async fetchCoupons() {
      try {
        // 1. 优先从vuex
        let userId = this.vuexUser && this.vuexUser.id;
        // 2. 解析token
        if (!userId) {
          const userString = localStorage.getItem('user');
          if (userString) {
            try {
              const userData = JSON.parse(userString);
              if (userData.token) {
                const tokenParts = userData.token.split('.');
                if (tokenParts.length === 3) {
                  const payload = JSON.parse(atob(tokenParts[1]));
                  userId = payload.userId || payload.id || payload.sub;
                }
              }
            } catch (e) {
              console.error('解析token失败:', e);
            }
          }
        }
        // 3. 兼容老逻辑
        if (!userId) {
          userId = localStorage.getItem('userId');
        }
        console.log('最终userId:', userId);
        if (!userId) {
          ElMessage.error('请先登录');
          this.$router.push('/login');
          return;
        }
        const response = await axios.get('/api/coupons/my-coupons', {
          headers: { userId }
        });
        this.coupons = response.data;
      } catch (error) {
        console.error('获取优惠券失败:', error);
        ElMessage.error('获取优惠券失败');
      } finally {
        this.loading = false;
      }
    },
    getCouponTypeText(type) {
      const typeMap = {
        'FIXED': '满减券',
        'DISCOUNT': '折扣券',
        'NEW_USER': '新人券'
      };
      return typeMap[type] || type;
    },
    getStatusText(status) {
      const statusMap = {
        '0': '未使用',
        '1': '已使用',
        '2': '已过期'
      };
      return statusMap[status?.toString()] || '未知状态';
    },
    getCouponStatusClass(coupon) {
      return {
        'used': coupon.status === '1' || coupon.status === 1,
        'expired': coupon.status === '2' || coupon.status === 2
      };
    },
    formatDate
  }
};
</script>

<style scoped>
.my-coupons {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading, .no-coupons {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-coupons p {
  font-size: 16px;
  color: #999;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.coupon-card {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  transition: transform 0.3s ease;
}

.coupon-card:hover {
  transform: translateY(-5px);
}

.coupon-card.used {
  background: linear-gradient(135deg, #bdc3c7, #95a5a6);
}

.coupon-card.expired {
  background: linear-gradient(135deg, #95a5a6, #7f8c8d);
}

.coupon-left {
  flex: 0 0 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
}

.coupon-amount {
  font-size: 24px;
  font-weight: bold;
}

.coupon-amount .currency {
  font-size: 16px;
}

.coupon-type {
  margin-top: 10px;
  font-size: 14px;
}

.coupon-right {
  flex: 1;
  padding: 20px;
}

.coupon-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.coupon-desc {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.coupon-conditions {
  font-size: 12px;
  margin-bottom: 10px;
  opacity: 0.8;
}

.coupon-validity {
  font-size: 12px;
  margin-bottom: 10px;
  opacity: 0.8;
}

.coupon-status {
  font-size: 14px;
  font-weight: bold;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(255, 255, 255, 0.3);
}
</style> 