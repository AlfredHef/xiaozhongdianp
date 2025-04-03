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
    async login(username, password, captchaId = null, captchaText = null) {
        try {
            console.log('【AuthService】尝试登录:', username);
            // 准备请求数据
            const requestData = {
                username,
                password
            };
            
            // 如果提供了验证码信息，则添加到请求中
            if (captchaId && captchaText) {
                requestData.captchaId = captchaId;
                requestData.captchaText = captchaText;
            }
            
            const response = await axios.post(`${API_URL}/user/login`, requestData);
            console.log('【AuthService】登录响应:', response);

            // 校验状态码和响应数据
            if (response.status === 200 && response.data) {
                // 兼容后端不同的数据结构
                let token = null;
                
                // 如果响应格式为data字段中包含token
                if (response.data.data) {
                    const data = response.data.data;
                    if (typeof data === 'string' && data.startsWith("Bearer ")) {
                        token = data.substring(7); // 提取 Token
                    } else if (data.token) {
                        token = data.token;
                    }
                } 
                // 如果响应直接包含token
                else if (response.data.token) {
                    token = response.data.token;
                }
                
                if (token) {
                    // 如果需要，这里可以添加保存到Vuex的代码
                    if (window && window.$store) {
                        console.log('【AuthService】保存token到Vuex');
                        window.$store.dispatch('auth/saveToken', token);
                        window.$store.dispatch('auth/saveUser', { username });
                    }
                    
                    return { 
                        success: true,
                        token, 
                        username 
                    };
                }
                
                // 如果没找到token但状态码正确，可能是其他成功情况
                if (response.data.code === 200 || response.data.success) {
                    return {
                        success: true,
                        message: response.data.message || '登录成功'
                    };
                }
                
                throw new Error(response.data.message || '登录失败，返回的数据格式不正确');
            } else {
                throw new Error(`登录失败，状态码：${response.status}`);
            }
        } catch (error) {
            console.error('Login error:', error);
            // 格式化错误信息
            const errorMsg = error.response?.data?.message || 
                            error.response?.data?.msg || 
                            error.message || 
                            '登录失败';
            return {
                success: false,
                message: errorMsg
            };
        }
    },

    //****// 获取存储的用户信息（如 token）
    getUser() {
        // 不从localStorage获取敏感信息
        // 改为从Vuex或其他安全存储机制获取
        try {
            return this._secureGetUserData();
        } catch (e) {
            console.error('获取用户数据失败:', e);
            return null;
        }
    },

    // 新增一个内部方法用于安全获取用户数据
    _secureGetUserData() {
        // 从Vuex中获取用户信息
        if (window && window.$store) {
            // 获取认证状态
            const isAuthenticated = window.$store.getters['auth/isAuthenticated'];
            // 获取用户信息
            const user = window.$store.getters['auth/user'];
            
            console.log('从Vuex获取认证状态:', isAuthenticated, '用户信息:', user);
            
            // 如果已认证且有用户信息，返回用户信息
            if (isAuthenticated && user) {
                return user;
            }
            // 如果已认证但没有用户信息，返回一个基本的用户对象
            else if (isAuthenticated) {
                return { authenticated: true };
            }
        }
        return null;
    },

    // 注销方法，清除 Token
    logout() {
        // 不再使用localStorage
        // localStorage.removeItem('user');
        // 改用更安全的方式处理登出
        if (window && window.$store) {
            window.$store.dispatch('auth/logout');
        }
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
