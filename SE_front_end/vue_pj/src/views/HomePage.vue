<template>
  <div id="app">
    <h1>{{ message }}</h1>
    <button @click="order">点单</button>
    <button @click="evaluate">评价</button>
    <button @click="quit">退出登录</button>
  </div>
</template>

<script>
import { styleType } from 'element-plus/es/components/table-v2/src/common';

export default {
  data() {
    return {
      message: '欢迎来到软旦餐厅主页！'

    };
  },
  methods: {
    order() {
      this.message = '点单模式';
    },
    evaluate() {
      this.message = '评价模式';
    },
    quit(){
      this.$router.push("/");  
    }
  }
};
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

button {
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  margin: 20px;
}

body {
  background-image: url('/background_homepage.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

</style>