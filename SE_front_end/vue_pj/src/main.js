import router from './router';
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 创建应用实例
const app = createApp(App);

// 注册插件
app.use(router);
app.use(ElementPlus, {
    locale: zhCn,
});

// 挂载应用（只调用一次mount）
app.mount('#app');
