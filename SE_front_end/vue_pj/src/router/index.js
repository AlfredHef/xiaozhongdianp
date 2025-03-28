import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import HomePage from '../views/HomePage.vue';
import ShopSearch from '../views/ShopSearch.vue';
import ShopDetail from '../views/ShopDetail.vue';

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: LoginPage },
  { path: '/register', name: 'Register', component: RegisterPage },
  { path: '/homepage', name: 'Homepage', component: HomePage },
  { path: '/shop/search', name: 'ShopSearch', component: ShopSearch },
  { path: '/shop/detail/:id', name: 'ShopDetail', component: ShopDetail }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
