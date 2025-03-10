<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="bg-white p-8 shadow-lg rounded-lg w-96">
      <h2 class="text-2xl font-bold text-gray-800 text-center">用户注册</h2>

      <div class="mt-4">
        <label class="block text-gray-600">用户名</label>
        <input type="text" v-model="username" placeholder="输入用户名"
               class="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
        <p v-if="usernameError" class="text-red-500 text-sm mt-1">{{ usernameError }}</p>
      </div>

      <div class="mt-4">
        <label class="block text-gray-600">密码</label>
        <input type="password" v-model="password" placeholder="输入密码"
               class="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" @input="checkPasswordStrength" />
        <p class="text-sm mt-1" :class="passwordStrengthClass">{{ passwordStrength }}</p>
      </div>

      <div class="mt-4">
        <label class="block text-gray-600">确认密码</label>
        <input type="password" v-model="confirmPassword" placeholder="确认密码"
               class="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
      </div>

      <button @click="register"
              class="w-full mt-6 bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg transition-all">注册</button>

      <p class="text-center text-gray-600 mt-4 text-sm">
        已有账号？<router-link to="/login" class="text-blue-500 hover:underline">去登录</router-link>
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
      confirmPassword: "",
      usernameError: "",
      passwordStrength: "",
      passwordStrengthClass: "text-gray-500",
    };
  },
  methods: {
    async register() {
      if (this.password !== this.confirmPassword) {
        alert("两次密码输入不一致！");
        return;
      }
      try {
        await AuthService.register(this.username, this.password);
        alert("注册成功，请登录！");
        this.$router.push("/login");
      } catch (error) {
        alert("注册失败，请重试！");
      }
    },
    checkPasswordStrength() {
      if (this.password.length < 6) {
        this.passwordStrength = "密码太短";
        this.passwordStrengthClass = "text-red-500";
      } else if (/\d/.test(this.password) && /[a-zA-Z]/.test(this.password)) {
        this.passwordStrength = "密码强度：中";
        this.passwordStrengthClass = "text-yellow-500";
      } else if (this.password.length >= 10) {
        this.passwordStrength = "密码强度：强";
        this.passwordStrengthClass = "text-green-500";
      } else {
        this.passwordStrength = "密码强度：弱";
        this.passwordStrengthClass = "text-red-500";
      }
    }
  },
};
</script>

<style scoped>
/* 可以使用 Tailwind CSS, 也可以在这里手动调整一些样式 */
</style>
