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
  
  // 如果响应包含dishItems，记录详细信息
  if (response.data && response.data.dishItems && Array.isArray(response.data.dishItems)) {
    console.log('响应中的菜品信息:');
    response.data.dishItems.forEach((item, index) => {
      console.log(`菜品 ${index+1}:`, {
        ...item,
        dishPrice_type: typeof item.dishPrice,
        dishDescription_type: typeof item.dishDescription
      });
    });
  }
  
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
      
      // 处理数据，确保dishItems中的dishPrice是数字，dishDescription是字符串
      if (response.data && response.data.dishItems && Array.isArray(response.data.dishItems)) {
        response.data.dishItems = response.data.dishItems.map(item => ({
          ...item,
          dishPrice: typeof item.dishPrice === 'number' ? item.dishPrice : parseFloat(item.dishPrice) || 0,
          dishDescription: item.dishDescription || ''
        }));
      }
      
      return response.data;
    } catch (error) {
      console.error('获取团购套餐详情失败:', error);
      throw error;
    }
  }

  // Create an order
  async createOrder(packageId, couponId = null) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.post(`${API_URL}/orders`, 
        { packageId, couponId }, 
        { headers: { 'userId': user.id } }
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
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/orders`, {
        headers: { 'userId': user.id }
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
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/orders/${orderId}`, {
        headers: { 'userId': user.id }
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
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/coupons/user`, {
        headers: { 'userId': user.id }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user coupons:', error);
      throw error;
    }
  }
}

export default new GroupBuyService(); 