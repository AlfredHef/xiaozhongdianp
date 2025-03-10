<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <form @submit.prevent="login">
      <label>用户名:</label>
      <input type="text" v-model="username" required />

      <label>密码:</label>
      <input type="password" v-model="password" required />

      <button type="submit">登录</button>
      <span v-if="loginError" class="error">{{ loginError }}</span>
    </form>
  </div>
</template>

<script>
import AuthService from "@/services/AuthService";

export default {
  data() {
    return {
      username: "",
      password: "",
      loginError: "",
    };
  },
  methods: {
    async login() {
      try {
        const response = await AuthService.login(this.username, this.password);
        if (response.success) {
          localStorage.setItem("token", response.token);
          this.$router.push("/");
        } else {
          this.loginError = response.message;
        }
      } catch (error) {
        this.loginError = "登录失败，请检查用户名和密码";
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: auto;
  padding: 20px;
}
.error {
  color: red;
}
</style>
