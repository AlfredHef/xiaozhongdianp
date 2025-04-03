import { createStore } from 'vuex';
import auth from './modules/auth';

// 创建Vuex store
export default createStore({
  modules: {
    auth
  }
}); 