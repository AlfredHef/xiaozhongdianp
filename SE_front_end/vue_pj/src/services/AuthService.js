import axios from 'axios';

const API_URL = "http://localhost:8080/api/auth"; // 后端接口 URL

export default {
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha`, { responseType: 'arraybuffer' });
            const captchaImage = `data:image/png;base64,${Buffer.from(response.data, 'binary').toString('base64')}`;
            return {
                captchaImage,  // 返回图片的 base64 字符串
                captchaId: response.data.captchaId // 返回验证码的 ID
            };
        } catch (error) {
            console.error('Error fetching captcha:', error);
            throw new Error('Failed to load captcha');
        }
    },

    // 验证验证码
    async verifyCaptcha(captchaInput, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/verify-captcha`, { captchaInput, captchaId });
            return response.data; // 返回成功的响应
        } catch (error) {
            console.error('Captcha verification error:', error.response?.data?.message || error.message);
            throw new Error('Captcha verification failed');
        }
    },

    // 登录接口，传递验证码
    async login(username, password, captcha, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/login`, { username, password, captcha, captchaId });
            return response.data; // 返回后端响应的数据（如 token）
        } catch (error) {
            console.error('Login error:', error.response?.data?.message || error.message);
            throw new Error('Login failed');
        }
    },

    // 注册接口，传递验证码
    async register(username, password, captcha) {
        try {
            const response = await axios.post(`${API_URL}/register`, { username, password, captcha });
            return response.data; // 注册成功返回的数据
        } catch (error) {
            console.error('Register error:', error.response?.data?.message || error.message);
            throw new Error('Registration failed');
        }
    }
};
