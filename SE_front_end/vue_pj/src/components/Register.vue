<template>
  <div>
    <h2>用户注册</h2>
    <form @submit.prevent="register">
      <label>用户名:</label>
      <input type="text" v-model="username" @blur="checkUsername">
      <span v-if="usernameError" class="error">{{ usernameError }}</span><br>

      <label>密码:</label>
      <input type="password" v-model="password">
      <span v-if="passwordError" class="error">{{ passwordError }}</span><br>

      <label>验证码:</label>
      <input type="text" v-model="captcha">
      <img :src="captchaUrl" @click="refreshCaptcha"><br>

      <button type="submit">注册</button>
      <span v-if="registerError" class="error">{{ registerError }}</span>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      captcha: '',
      captchaUrl: '/api/captcha',  // 后端生成验证码的API路径，需替换为实际路径
      usernameError: '',
      passwordError: '',
      registerError: ''
    };
  },
  methods: {
    checkUsername() {
      // 实时校验用户名
      // 实现用户名合法性校验逻辑，例如长度和字符规则等
    },
    refreshCaptcha() {
      // 刷新验证码图片
      this.captchaUrl = '/api/captcha?' + new Date().getTime();
    },
    register() {
      // 提交注册表单，调用后端API
      // 发送 this.username, this.password, this.captcha 到后端
      // 处理后端返回的注册结果，例如显示错误信息
    }
  }
};
</script>

<style scoped>
.error {
  color: red;
}
</style>
