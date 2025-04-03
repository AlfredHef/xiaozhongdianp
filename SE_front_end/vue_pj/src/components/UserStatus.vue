<template>
  <div class="user-status" v-if="shouldShow">
    <div class="nav-links">
      <el-button link @click="$router.push('/shop/list')">商家列表</el-button>
    </div>
    <div v-if="isLoggedIn" class="user-info">
      <span>你好,{{ username }}</span>
      <el-dropdown trigger="click">
        <el-avatar :size="32" :src="defaultAvatar"></el-avatar>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="viewProfile">个人资料</el-dropdown-item>
            <el-dropdown-item @click="viewOrders">我的订单</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div v-else class="login-links">
      <el-button link @click="$router.push('/login')">登录</el-button>
      <el-button link @click="$router.push('/register')">注册</el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import AuthService from '@/services/AuthService';

export default {
  name: 'UserStatus',
  props: {
    // 通过props接收当前路由路径
    currentPath: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const router = useRouter();
    const store = useStore();
    const user = ref(null);
    const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    
    // 从Vuex获取认证状态和用户信息
    const isAuthenticated = computed(() => store.getters['auth/isAuthenticated']);
    const storeUser = computed(() => store.getters['auth/user']);
    
    // 计算属性：是否已登录（同时检查Vuex和本地状态）
    const isLoggedIn = computed(() => isAuthenticated.value || !!user.value);
    
    // 计算属性：用户名
    const username = computed(() => {
      // 优先使用Vuex中的用户信息
      if (storeUser.value && storeUser.value.username) {
        return storeUser.value.username;
      }
      // 其次使用组件本地的用户信息
      if (user.value && user.value.username) {
        return user.value.username;
      }
      // 如果都没有，返回空字符串
      return '用户';
    });
    
    // 计算属性：是否应该显示用户状态
    const shouldShow = computed(() => {
      // 确保currentPath有值且不是登录或注册页面
      if (!props.currentPath) return true; // 默认显示
      return props.currentPath !== '/login' && props.currentPath !== '/register';
    });
    
    // 初始化时获取用户信息
    onMounted(() => {
      loadUserInfo();
      
      // 添加自定义事件监听
      window.addEventListener('update-user-status', () => {
        console.log('收到更新用户状态事件');
        loadUserInfo();
      });
      
      // 定期检查用户状态（可选，用于确保页面刷新后能获取到最新状态）
      setInterval(() => {
        console.log('定期检查用户状态...');
        loadUserInfo();
      }, 3000);
    });
    
    // 组件卸载时清除事件监听
    onUnmounted(() => {
      window.removeEventListener('update-user-status', loadUserInfo);
    });
    
    // 监听Vuex中的认证状态变化
    watch(isAuthenticated, (newValue) => {
      console.log('认证状态变化:', newValue);
      if (newValue) {
        // 认证状态变为已登录，重新加载用户信息
        loadUserInfo();
      } else {
        // 认证状态变为未登录，清除用户信息
        user.value = null;
      }
    });
    
    // 加载用户信息
    const loadUserInfo = () => {
      // 从AuthService获取用户信息
      const serviceUser = AuthService.getUser();
      
      // 如果AuthService有用户信息，使用它
      if (serviceUser) {
        user.value = serviceUser;
      } 
      // 否则，如果Vuex有用户信息，使用Vuex中的用户信息
      else if (storeUser.value) {
        user.value = storeUser.value;
      }
      
      console.log('当前登录用户信息:', user.value, '认证状态:', isAuthenticated.value);
    };
    
    // 退出登录
    const logout = () => {
      // 调用AuthService的登出方法（该方法内部会清除Vuex状态）
      AuthService.logout();
      // 清除本地状态
      user.value = null;
      // 跳转到登录页
      router.push('/login');
    };
    
    // 查看个人资料
    const viewProfile = () => {
      router.push('/profile');
    };
    
    // 查看订单
    const viewOrders = () => {
      router.push('/orders');
    };
    
    return {
      isLoggedIn,
      username,
      defaultAvatar,
      logout,
      viewProfile,
      viewOrders,
      shouldShow
    };
  }
};
</script>

<style scoped>
.user-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 16px;
  gap: 15px;
}

.nav-links {
  margin-right: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info span {
  font-size: 14px;
  color: #606266;
}

.login-links {
  display: flex;
  gap: 10px;
}
</style> 