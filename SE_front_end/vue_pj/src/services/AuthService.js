import axios from "axios";

const API_URL = "http://localhost:8088/user";  // 修改端口和路径
 // 确保后端端口正确

export default {
    async login(username, password) {
        return axios.post(`${API_URL}/login`, { username, password });
    },
    async register(username, password) {
        return axios.post(`${API_URL}/register`, { username, password });
    }
};
