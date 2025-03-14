import axios from 'axios';

const API_URL = "http://localhost:8088";  // 修改端口和路径，确保后端端口正确

export default {
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha/generate`);
            // 从响应体中获取 captchaId 和 captchaImage（Base64 编码）
            const { captchaImage, captchaId } = response.data;

            // 返回验证码图片和验证码 ID
            return {
                captchaImage,  // Base64 编码的验证码图片
                captchaId      // 验证码 ID
            };
        } catch (error) {
            console.error('Error fetching captcha:', error);
            throw new Error('Failed to load captcha');
        }
    },

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
    async register(username, password, captchaText, captchaId) {
        // 打印出构建的请求体，确保数据结构正确
        console.log('Register payload:', {
            user: {
                username,
                password
            },
            captchaId,
            captchaText
        });

        try {
            // 构建请求体
            const payload = {
                user: {
                    username,
                    password
                },
                captchaId,
                captchaText
            };

            // 发送 POST 请求
            const response = await axios.post(`${API_URL}/user/register`, payload);

            // 返回注册成功的响应数据
            return response.data;
        } catch (error) {
            console.error('Register error:', error.response?.data?.message || error.message);
            throw new Error('Registration failed');
        }
    }

};
