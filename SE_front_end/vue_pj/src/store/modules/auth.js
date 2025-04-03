// 用于安全管理用户认证状态的Vuex模块

// 从localStorage获取初始状态
const getInitialState = () => {
  try {
    const token = localStorage.getItem('auth_token');
    const userStr = localStorage.getItem('auth_user');
    const user = userStr ? JSON.parse(userStr) : null;
    return {
      token: token || null,
      user: user,
      isAuthenticated: !!token
    };
  } catch (e) {
    console.error('从localStorage获取认证状态失败:', e);
    return {
      token: null,
      user: null,
      isAuthenticated: false
    };
  }
};

export default {
  namespaced: true,
  
  // 使用函数返回初始状态
  state: getInitialState,
  
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      state.isAuthenticated = !!token;
      // 持久化存储token
      if (token) {
        localStorage.setItem('auth_token', token);
      } else {
        localStorage.removeItem('auth_token');
      }
    },
    
    SET_USER(state, user) {
      state.user = user;
      // 持久化存储用户信息
      if (user) {
        localStorage.setItem('auth_user', JSON.stringify(user));
      } else {
        localStorage.removeItem('auth_user');
      }
    },
    
    LOGOUT(state) {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      // 清除持久化存储
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
    },
  },
  
  actions: {
    // 初始化认证状态
    initAuth({ commit }) {
      const token = localStorage.getItem('auth_token');
      const userStr = localStorage.getItem('auth_user');
      
      if (token) {
        commit('SET_TOKEN', token);
      }
      
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          commit('SET_USER', user);
        } catch (e) {
          console.error('解析用户信息失败:', e);
          // 如果解析失败，清除可能损坏的数据
          localStorage.removeItem('auth_user');
        }
      }
    },
    
    // 保存令牌到Vuex状态和localStorage
    saveToken({ commit }, token) {
      commit('SET_TOKEN', token);
    },
    
    // 保存用户信息到Vuex状态和localStorage
    saveUser({ commit }, user) {
      commit('SET_USER', user);
    },
    
    // 登出动作
    logout({ commit }) {
      commit('LOGOUT');
    }
  },
  
  getters: {
    isAuthenticated: state => state.isAuthenticated,
    token: state => state.token,
    user: state => state.user
  }
}; 