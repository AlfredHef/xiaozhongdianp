import axios from 'axios';

const API_URL = "http://localhost:8088";  // 修改端口和路径
 // 确保后端端口正确

export default {
    // 获取验证码图片
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha/generate`, {
                responseType: 'arraybuffer'
            });
            // 浏览器环境处理二进制数据
            const blob = new Blob([response.data], { type: 'image/png' });
            const captchaImage = URL.createObjectURL(blob);
            // 从响应头获取 captchaId（注意响应头大小写，后端可能是驼峰或全小写）
            const captchaId = response.headers['captcha-id'] || response.headers['Captcha-Id'];
            return {
                captchaImage,
                captchaId
            };
        } catch (error) {
            console.error('Error fetching captcha:', error);
            throw new Error('Failed to load captcha');
        }
    },

    // 验证验证码
    // 验证验证码
    async verifyCaptcha(captchaText, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/captcha/verify`,
                null, // 空请求体
                {
                    params: { captchaId, captchaText } // 通过 params 传递查询参数
                }
            );
            return response.data;
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
