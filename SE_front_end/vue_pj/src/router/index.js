import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import ShopSearch from '../views/ShopSearch.vue';
import ShopDetail from '../views/ShopDetail.vue';
import ShopList from '../views/ShopList.vue';
import AuthService from '../services/AuthService';

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: LoginPage },
  { path: '/register', name: 'Register', component: RegisterPage },
  { path: '/shop/search', name: 'ShopSearch', component: ShopSearch, meta: { requiresAuth: true } },
  { path: '/shop/detail/:id', name: 'ShopDetail', component: ShopDetail, meta: { requiresAuth: true } },
  { path: '/shop/list', name: 'ShopList', component: ShopList, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 检查路由是否需要身份验证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  // 当前用户是否已登录
  const isLoggedIn = !!AuthService.getUser();
  
  // 如果需要身份验证且用户未登录，则重定向到登录页面
  if (requiresAuth && !isLoggedIn) {
    next({ name: 'Login' });
  } else {
    next(); // 其他情况正常导航
  }
});

export default router;
