import axios from 'axios';
import AuthService from './AuthService';

const API_URL = 'http://localhost:8088/api';

class GroupBuyService {
  // Get packages by shop ID
  async getPackagesByShopId(shopId) {
    try {
      const token = AuthService.getToken();
      const response = await axios.get(`${API_URL}/groupbuy/packages?shopId=${shopId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching packages:', error);
      throw error;
    }
  }

  // Get package detail by ID
  async getPackageDetail(packageId) {
    try {
      const token = AuthService.getToken();
      const response = await axios.get(`${API_URL}/groupbuy/packages/${packageId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching package detail:', error);
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