import axios from 'axios';
import AuthService from './AuthService';

const API_URL = "http://localhost:8088";

// 用户名缓存，避免重复请求
const usernameCache = new Map();

// 简单的用户信息服务
class UserService {
  /**
   * 根据用户ID获取用户名
   * @param {number} userId 用户ID
   * @returns {Promise<string>} 用户名
   */
  async getUsernameById(userId) {
    if (!userId) return '未知用户';
    
    // 获取当前登录用户
    const currentUser = AuthService.getUser();
    
    // 如果是当前登录用户，返回当前用户名
    if (currentUser && currentUser.id == userId) {
      return currentUser.username || '我';
    }
    
    // 检查缓存
    if (usernameCache.has(userId)) {
      console.log(`从缓存中获取用户名：${usernameCache.get(userId)}`);
      return usernameCache.get(userId);
    }
    
    try {
      // 调用后端API获取用户信息
      const response = await axios.get(`${API_URL}/user/${userId}`);
      
      if (response.data && response.data.code === 200 && response.data.data) {
        const username = response.data.data.username;
         console.log(`获取用户名：${username}`);
        // 缓存用户名
        usernameCache.set(userId, username);
        return username;
      } else {
        // API调用失败，返回默认格式
        const fallbackName = `用户${userId}`;
        usernameCache.set(userId, fallbackName);
        return fallbackName;
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 发生错误时，返回默认格式并缓存
      const fallbackName = `用户${userId}`;
      usernameCache.set(userId, fallbackName);
      return fallbackName;
    }
  }
  
  /**
   * 清除用户名缓存
   */
  clearCache() {
    usernameCache.clear();
  }
  
  /**
   * 同步方法：根据用户ID获取用户名（使用缓存，如果没有缓存则返回默认值）
   * @param {number} userId 用户ID
   * @returns {string} 用户名
   */
  getUsernameByIdSync(userId) {
    if (!userId) return '未知用户';
    
    // 获取当前登录用户
    const currentUser = AuthService.getUser();
    
    // 如果是当前登录用户，返回当前用户名
    if (currentUser && currentUser.id == userId) {
      return currentUser.username || '我';
    }
    
    // 检查缓存
    if (usernameCache.has(userId)) {
      return usernameCache.get(userId);
    }
    
    // 如果没有缓存，异步获取并返回默认值
    this.getUsernameById(userId); // 异步获取并缓存
    return `用户${userId}`;
  }
}

export default new UserService(); 