import axios from 'axios';

const API_URL = "http://localhost:8088";  // 修改端口和路径
 // 确保后端端口正确

export default {
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha/generate`, { responseType: 'arraybuffer' });
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
    async verifyCaptcha(captchaText, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/captcha/verify`, { captchaText, captchaId });
            return response.data; // 返回成功的响应
        } catch (error) {
            console.error('Captcha verification error:', error.response?.data?.message || error.message);
            throw new Error('Captcha verification failed');
        }
    },

    // 登录接口，传递验证码
    async login(username, password, captchaText, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/user/login`, { username, password, captcha: captchaText, captchaId });
            return response.data; // 返回后端响应的数据（如 token）
        } catch (error) {
            console.error('Login error:', error.response?.data?.message || error.message);
            throw new Error('Login failed');
        }
    },

    // 注册接口，传递验证码
    async register(username, password, captcha, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/user/register`, { username, password, captcha, captchaId });
            return response.data; // 注册成功返回的数据
        } catch (error) {
            console.error('Register error:', error.response?.data?.message || error.message);
            throw new Error('Registration failed');
        }
    }
};
