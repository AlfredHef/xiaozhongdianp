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
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/services/AuthService';
import { USER_EVENTS, addEventListener } from '@/utils/sessionState';
import { useStore } from 'vuex';

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
    
    // 计算属性：是否已登录 - 优先使用Vuex状态
    const isLoggedIn = computed(() => {
      return store.getters['auth/isAuthenticated'] || !!user.value;
    });
    
    // 计算属性：用户名 - 优先使用Vuex状态
    const username = computed(() => {
      const vuexUser = store.getters['auth/currentUser'];
      if (vuexUser) return vuexUser.username || '用户';
      if (!user.value) return '';
      return user.value.username || '用户';
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
      
      // 监听用户登录事件
      const loginUnsubscribe = addEventListener(USER_EVENTS.LOGIN, (userData) => {
        console.log('收到用户登录事件:', userData);
        user.value = userData;
      });
      
      // 监听用户登出事件
      const logoutUnsubscribe = addEventListener(USER_EVENTS.LOGOUT, () => {
        console.log('收到用户登出事件');
        user.value = null;
      });
      
      // 监听用户更新事件
      const updateUnsubscribe = addEventListener(USER_EVENTS.UPDATE, (userData) => {
        console.log('收到用户信息更新事件:', userData);
        user.value = userData;
      });
      
      // 组件卸载时清除监听器
      onUnmounted(() => {
        loginUnsubscribe();
        logoutUnsubscribe();
        updateUnsubscribe();
      });
      
      // 初始化Vuex认证状态
      if (store && store.dispatch) {
        store.dispatch('auth/initAuth');
      }
    });
    
    // 加载用户信息
    const loadUserInfo = () => {
      // 优先从Vuex状态获取
      const vuexUser = store.getters['auth/currentUser'];
      if (vuexUser) {
        user.value = vuexUser;
        console.log('从Vuex获取用户信息:', user.value);
        return;
      }
      
      // 如果Vuex中没有，尝试从AuthService获取
      user.value = AuthService.getUser();
      if (user.value) {
        console.log('从AuthService获取用户信息:', user.value);
        // 更新Vuex状态
        if (store && store.dispatch) {
          store.dispatch('auth/saveUser', user.value);
        }
      } else {
        console.log('未获取到用户信息');
      }
    };
    
    // 退出登录
    const logout = () => {
      // 使用Vuex action处理登出
      if (store && store.dispatch) {
        store.dispatch('auth/logout');
      } else {
        AuthService.logout();
      }
      user.value = null;
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
      shouldShow,
      loadUserInfo
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