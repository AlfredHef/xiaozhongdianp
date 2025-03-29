import axios from 'axios';

const API_URL = "http://localhost:8088";  // 确保与后端端口一致

export default {
    /**
     * 获取商家列表
     * @param {number} pageCurrent - 当前页码
     * @param {number} pageSize - 每页条数
     * @returns {Promise<Object>} 商家列表数据
     */
    async getShops(pageCurrent, pageSize) {
        try {
            const response = await axios.get(`${API_URL}/shop/page`, {
                params: { pageCurrent, pageSize }
            });
            return response.data;
        } catch (error) {
            console.error('获取商家列表失败:', error);
            throw error;
        }
    },

    /**
     * 搜索商家
     * @param {Object} queryParams - 搜索参数对象
     * @returns {Promise<Object>} 搜索结果
     */
    async searchShops(queryParams) {
        try {
            // 添加默认分页参数
            const params = {
                ...queryParams,
                pageSize: queryParams.pageSize || 10,
                pageCurrent: queryParams.pageCurrent || 1
            };
            console.log('发送搜索请求，参数:', params); // 添加日志
            const response = await axios.get(`${API_URL}/shop/search`, { params });
            console.log('搜索响应:', response.data); // 添加日志
            
            if (response.data.code === 1 && (!response.data.data || response.data.data.length === 0)) {
                console.log('未找到符合条件的商家，尝试检查数据库中是否有相关数据');
            }
            
            return response.data;
        } catch (error) {
            console.error('搜索商家失败:', error);
            throw error;
        }
    },

    /**
     * 获取搜索历史
     * @param {number} userId - 用户ID
     * @returns {Promise<Object>} 搜索历史记录
     */
    async getSearchHistory(userId) {
        try {
            const response = await axios.get(`${API_URL}/shop/search/history`, {
                params: { userId }
            });
            return response.data;
        } catch (error) {
            console.error('获取搜索历史失败:', error);
            throw error;
        }
    },

    /**
     * 获取商家详情
     * @param {number} shopId - 商家ID
     * @returns {Promise<Object>} 商家详情数据
     */
    async getShopDetails(shopId) {
        try {
            const response = await axios.get(`${API_URL}/shop/details/${shopId}`);
            return response.data;
        } catch (error) {
            console.error('获取商家详情失败:', error);
            throw error;
        }
    }
} 