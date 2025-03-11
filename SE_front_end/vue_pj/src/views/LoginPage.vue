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
          <div class="captcha-image" @click="refreshCaptcha" v-html="captchaImage"></div>
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
      captchaText: "",
      captchaImage: "",
      captchaError: ""
    };
  },
  mounted() {
    this.generateCaptcha();
  },
  methods: {
    async login() {
      // 重置错误信息
      this.captchaError = "";

      // 验证验证码
      if (this.captchaInput.toLowerCase() !== this.captchaText.toLowerCase()) {
        this.captchaError = "验证码不正确";
        return;
      }

      try {
        await AuthService.login(this.username, this.password);
        this.$router.push("/");
      } catch (error) {
        alert("登录失败，请检查用户名和密码！");
        // 刷新验证码
        this.refreshCaptcha();
      }
    },
    generateCaptcha() {
      // 生成随机验证码文本（4-6位字母数字组合）
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
      const length = Math.floor(Math.random() * 3) + 4; // 4-6位长度
      let result = '';
      for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      this.captchaText = result;

      // 生成SVG验证码图片
      this.captchaImage = this.createCaptchaSVG(result);
    },
    createCaptchaSVG(text) {
      const width = 120;
      const height = 40;

      // 开始SVG
      let svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">`;

      // 背景
      svg += `<rect width="100%" height="100%" fill="#f0f2f5" />`;

      // 添加干扰线
      for (let i = 0; i < 5; i++) {
        const x1 = Math.random() * width;
        const y1 = Math.random() * height;
        const x2 = Math.random() * width;
        const y2 = Math.random() * height;
        const color = `rgb(${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)})`;
        svg += `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${color}" stroke-width="1" />`;
      }

      // 添加干扰点
      for (let i = 0; i < 30; i++) {
        const cx = Math.random() * width;
        const cy = Math.random() * height;
        const color = `rgb(${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)})`;
        svg += `<circle cx="${cx}" cy="${cy}" r="1" fill="${color}" />`;
      }

      // 添加验证码文本
      const fontSize = Math.floor(height * 0.7);
      const letterSpacing = width / (text.length + 1);

      for (let i = 0; i < text.length; i++) {
        const x = letterSpacing * (i + 0.5);
        const y = height / 2 + fontSize / 3;
        const rotate = Math.random() * 30 - 15; // -15到15度的随机旋转
        const color = `rgb(${Math.floor(Math.random() * 100)}, ${Math.floor(Math.random() * 100)}, ${Math.floor(Math.random() * 100)})`;

        svg += `<text x="${x}" y="${y}" font-family="Arial" font-size="${fontSize}" fill="${color}"
                  transform="rotate(${rotate}, ${x}, ${y})">${text[i]}</text>`;
      }

      // 结束SVG
      svg += '</svg>';

      return svg;
    },
    refreshCaptcha() {
      this.generateCaptcha();
      this.captchaInput = "";
      this.captchaError = "";
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