import axios from 'axios';
import AuthService from './AuthService';

const API_URL = 'http://localhost:8088/api';

// 添加请求拦截器
axios.interceptors.request.use(config => {
  console.log('发送请求:', {
    url: config.url,
    method: config.method,
    headers: config.headers,
    params: config.params
  });
  return config;
});

// 添加响应拦截器
axios.interceptors.response.use(response => {
  console.log('收到响应:', {
    status: response.status,
    data: response.data
  });
  return response;
}, error => {
  console.error('请求错误:', {
    status: error.response?.status,
    data: error.response?.data,
    message: error.message
  });
  return Promise.reject(error);
});

class GroupBuyService {
  // Get packages by shop ID
  async getPackagesByShopId(shopId) {
    try {
      console.log('开始获取团购套餐，shopId:', shopId);
      const user = AuthService.getUser();
      console.log('当前用户信息:', user);
      
      if (!user || !user.token) {
        console.error('未获取到用户信息或token');
        throw new Error('未登录或token已过期');
      }
      
      const url = `${API_URL}/groupbuy/packages?shopId=${shopId}`;
      console.log('请求URL:', url);
      
      const response = await axios.get(url, {
        headers: { 'Authorization': `Bearer ${user.token}` }
      });
      console.log('团购套餐响应:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取团购套餐失败:', error);
      throw error;
    }
  }

  // Get package detail by ID
  async getPackageDetail(packageId) {
    try {
      console.log('开始获取团购套餐详情，packageId:', packageId);
      const url = `${API_URL}/groupbuy/packages/${packageId}`;
      console.log('请求URL:', url);
      
      const response = await axios.get(url);
      console.log('团购套餐详情响应:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取团购套餐详情失败:', error);
      throw error;
    }
  }

  // Create an order
  async createOrder(packageId, couponId = null) {
    try {
      const token = AuthService.getToken();
      const response = await axios.post(`${API_URL}/orders`, 
        { packageId, couponId }, 
        { headers: { 'Authorization': `Bearer ${token}` }}
      );
      return response.data;
    } catch (error) {
      console.error('Error creating order:', error);
      throw error;
    }
  }

  // Get user's orders
  async getUserOrders() {
    try {
      const token = AuthService.getToken();
      const response = await axios.get(`${API_URL}/orders/user`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user orders:', error);
      throw error;
    }
  }

  // Get order details with voucher
  async getOrderDetail(orderId) {
    try {
      const token = AuthService.getToken();
      const response = await axios.get(`${API_URL}/orders/${orderId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching order detail:', error);
      throw error;
    }
  }

  // Get user available coupons
  async getUserCoupons() {
    try {
      const token = AuthService.getToken();
      const response = await axios.get(`${API_URL}/coupons/user`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user coupons:', error);
      throw error;
    }
  }
}

export default new GroupBuyService(); 