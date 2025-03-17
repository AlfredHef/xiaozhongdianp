<template>
  <div class="container">
    <div class="form-card">
      <h2 class="form-title">用户登录</h2>

      <div class="form-group">
        <label class="form-label">用户名</label>
        <input type="text" v-model="username" placeholder="输入用户名" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">密码</label>
        <input type="password" v-model="password" placeholder="输入密码" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">验证码</label>
        <div class="captcha-container">
          <input type="text" v-model="captchaInput" placeholder="输入验证码" class="form-input captcha-input" />
          <img :src="captchaImage" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
        </div>
        <p v-if="captchaError" class="error-message">{{ captchaError }}</p>
      </div>

      <button @click="login" class="login-button">登录</button>

      <p class="register-link">
        还没有账号？<router-link to="/register" class="link">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import AuthService from "../services/AuthService";

export default {
  data() {
    return {
      username: "",
      password: "",
      captchaInput: "",
      captchaImage: "",
      captchaError: "",
      captchaId: "" // 存储验证码 ID
    };
  },
  mounted() {
    this.getCaptcha();  // 页面加载时获取验证码
  },
  //****
  methods: {
    // 登录时调用
    async login() {
      // 清除错误信息
      this.captchaError = "";

      // 验证验证码
      try {
        // 先验证验证码
        await AuthService.verifyCaptcha(this.captchaInput, this.captchaId); // 向后端验证验证码

        // 如果验证码验证通过，调用登录接口
        const loginResponse = await AuthService.login(this.username, this.password, this.captchaInput, this.captchaId);

        // 判断后端返回的 token 是否有效
        if (loginResponse && loginResponse.token) {
          // 存储 token 到 localStorage
          localStorage.setItem("user", JSON.stringify({ token: loginResponse.token }));
          // 跳转到首页
          this.$router.push("/homepage");
        } else {
          throw new Error("登录失败，未返回 token");
        }
      } catch (error) {
        alert("登录失败，请检查用户名、密码和验证码！");
        this.refreshCaptcha();  // 刷新验证码
      }
    },

    // 获取后端生成的验证码
    async getCaptcha() {
      try {
        const response = await AuthService.getCaptcha();  // 从后端获取验证码图片
        this.captchaImage = response.captchaImage;
        this.captchaId = response.captchaId;  // 保存验证码 ID
      } catch (error) {
        console.error("获取验证码失败：", error);
      }
    },

    // 刷新验证码
    refreshCaptcha() {
      this.getCaptcha();  // 刷新验证码图片
      this.captchaInput = "";  // 清空验证码输入框
      this.captchaError = "";  // 清空错误提示
    }
  }
};
</script>


<style scoped>
/*
  全局样式，对 body 和 html 元素进行样式设置。
  去除默认的外边距和内边距，使元素充满整个页面。
  设置高度和宽度为 100%，确保页面占满整个可视区域。
*/
body, html {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
/*
  对 body 元素设置背景样式。
  使用背景图片 "/Home_BG.jpg" 作为页面的背景。
  background-size: cover 使背景图片覆盖整个页面。
  background-position: center 使背景图片居中显示。
  background-repeat: no-repeat 防止背景图片重复显示。
  background-attachment: fixed 使背景图片固定，不随页面滚动而滚动。
*/
body {
  background-image: url('/Home_BG.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.form-card {
  background-color: white;
  padding: 2rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  border-radius: 0.5rem;
  width: 24rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  text-align: center;
}

.form-group {
  margin-top: 1rem;
}

.form-label {
  display: block;
  color: #4b5563;
  text-align:left;
}

.form-input {
  width: 100%;
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  margin-top: 0.25rem;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.login-button {
  width: 100%;
  margin-top: 1.5rem;
  background-color: #3b82f6;
  color: white;
  padding: 0.5rem 0;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #2563eb;
}

.register-link {
  text-align: center;
  color: #4b5563;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.link {
  color: #3b82f6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>