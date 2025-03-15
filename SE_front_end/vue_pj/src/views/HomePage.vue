<template>
  <div id="app">
    <h1>{{ message }}</h1>
    <button @click="changeMessage">点击我</button>
  </div>
</template>

<script>
import { styleType } from 'element-plus/es/components/table-v2/src/common';

export default {
  data() {
    return {
      message:'sty'

    };
  },
  methods: {
    changeMessage() {
      this.message = '你点击了按钮!';
    }
  }
};
</script>

<style>
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
}
</style>