// 用于安全管理用户认证状态的Vuex模块
export default {
  namespaced: true,
  state: {
    token: null,
    user: null,
    isAuthenticated: false,
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      state.isAuthenticated = !!token;
    },
    SET_USER(state, user) {
      state.user = user;
    },
    LOGOUT(state) {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
    },
  },
  actions: {
    // 保存令牌到Vuex状态中（内存中）
    saveToken({ commit }, token) {
      commit('SET_TOKEN', token);
    },
    // 保存用户信息
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