<template>
  <div id="app">
    <h1>{{ message }}</h1>
    <button @click="order">点单</button>
    <button @click="evaluate">评价</button>
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
  margin: 20px;
}

/*对 body 元素设置背景样式。
  使用背景图片 "/Home_BG.jpg" 作为页面的背景。
  background-size: cover 使背景图片覆盖整个页面。
  background-position: center 使背景图片居中显示。
  background-repeat: no-repeat 防止背景图片重复显示。
  background-attachment: fixed 使背景图片固定，不随页面滚动而滚动。
*/
body {
  background-image: url('/background_homepage.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}
</style>