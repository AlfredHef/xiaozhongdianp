import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import ShopSearch from '@/components/ShopSearch.vue';  
import HomePage from '../views/HomePage.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: LoginPage },
  { path: '/register', name: 'Register', component: RegisterPage },
  { 
    path: '/search', 
    name: 'ShopSearch', 
    component: ShopSearch,
    meta: { requiresAuth: true }
  },
  {
    path: '/shop/:id',
    name: 'ShopDetail',
    component: () => import('@/views/ShopDetail.vue'),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token'); // 检查是否已登录
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;
