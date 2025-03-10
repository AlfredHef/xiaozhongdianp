<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <input type="text" v-model="username" placeholder="输入用户名" />
    <input type="password" v-model="password" placeholder="输入密码" />
    <button @click="login">登录</button>
    <p>还没有账号？<router-link to="/register">去注册</router-link></p>
  </div>
</template>

<script>
import AuthService from "../services/AuthService";

export default {
  data() {
    return {
      username: "",
      password: "",
    };
  },
  methods: {
    async login() {
      try {
        await AuthService.login(this.username, this.password);
        this.$router.push("/");
      } catch (error) {
        alert("登录失败，请检查用户名和密码！");
      }
    },
  },
};
</script>
