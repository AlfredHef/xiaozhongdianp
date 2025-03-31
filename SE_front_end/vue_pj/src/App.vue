<template>
  <div class="app-container">
    <header class="app-header">
      <div class="logo">
        <router-link to="/">软旦餐厅</router-link>
      </div>
      <UserStatus :current-path="currentPath" />
    </header>
    <main class="app-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
import UserStatus from '@/components/UserStatus.vue';
import { useRoute, useRouter } from 'vue-router';
import { computed, ref, onMounted } from 'vue';

export default {
  name: 'App',
  components: {
    UserStatus
  },
  setup() {
    // 使用ref初始化currentPath，避免直接依赖$route
    const currentPath = ref('/');
    
    // 在onMounted生命周期钩子中安全地获取路由信息
    onMounted(() => {
      try {
        const router = useRouter();
        if (router) {
          // 监听路由变化
          router.afterEach((to) => {
            currentPath.value = to.path;
          });
          
          // 获取当前路径
          const route = useRoute();
          if (route && route.path) {
            currentPath.value = route.path;
          }
        }
      } catch (error) {
        console.error('路由初始化错误:', error);
      }
    });
    
    return { 
      currentPath
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f5f7fa;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;  /* 固定在页面顶部 */
  top: 0;           /* 顶部对齐 */
  left: 0;          /* 左边对齐 */
  right: 0;         /* 右边对齐 */
  z-index: 10;      /* 确保在其他元素之上 */
  box-sizing: border-box; /* 确保padding不会影响整体宽度 */
}

body {
  padding-top: 50px; /* 与header高度一致 */
  height: calc(100vh - 50px); /* 可选：根据需求决定是否设置 */
}


.logo {
  font-size: 24px;
  font-weight: bold;
}

.logo a {
  color: #409EFF;
  text-decoration: none;
}

.app-content {
  flex: 1;
}
</style>
