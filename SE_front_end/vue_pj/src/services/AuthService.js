import axios from 'axios';

const API_URL = "http://localhost:8088";  // 修改端口和路径，确保后端端口正确

export default {
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha/generate`);
            const { captchaImage, captchaId } = response.data;
            return { captchaImage, captchaId };
        } catch (error) {
            console.error('Error fetching captcha:', error);
            throw new Error('Failed to load captcha');
        }
    },

    // 验证验证码
    async verifyCaptcha(captchaText, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/captcha/verify`, null, {
                params: { captchaId, captchaText }
            });
            return response.data;
        } catch (error) {
            console.error('Captcha verification error:', error.response?.data?.message || error.message);
            throw new Error('Captcha verification failed');
        }
    },

    //****// 登录接口，传递验证码
    async login(username, password) {
        try {
            const response = await axios.post(`${API_URL}/user/login`, { username, password});

            // 判断后端是否返回 token 并处理
            if (response.data && response.data.data && response.data.data.startsWith("Bearer ")) {
                // 提取 Token (去除 'Bearer ' 前缀)
                const token = response.data.data.split("Bearer ")[1];
                // 将 Token 存储到 localStorage
                localStorage.setItem('user', JSON.stringify({ token }));
                return { token };  // 返回存储的 token
            } else {
                throw new Error('登录失败，未返回 Token');
            }
        } catch (error) {
            console.error('Login error:', error.response?.data?.message || error.message);
            throw new Error('Login failed');
        }
    },

    //****// 获取存储的用户信息（如 token）

    getUser() {
        return JSON.parse(localStorage.getItem('user'));
    },

    // 注销方法，清除 Token
    logout() {
        localStorage.removeItem('user');
    },

    // 注册接口，传递验证码
    // AuthService.js
    async register(username, password, captchaText, captchaId) {
        try {
        const payload = {
            user: { username, password },
            captchaId,
            captchaText
        };
  
        // 发送 POST 请求
        const response = await axios.post(`${API_URL}/user/register`, payload);
  
        // 直接根据后端的 code 判断是否成功
        if (response.data.code === 200) {
            return response.data;
        } else {
            // 如果 code 不是 200，抛出后端返回的 msg
            throw new Error(response.data.msg || '注册失败');
        }
        } catch (error) {
        // 如果是 HTTP 400 错误，提取后端的 msg
        if (error.response && error.response.data) {
            throw new Error(error.response.data.msg || '注册失败');
        } else {
            throw new Error(error.message || '注册失败，请重试！');
        }
        }
    }
}
