import axios from "axios";

const API_URL = "http://localhost:8080/api/auth"; // 确保后端端口正确

export default {
    async login(username, password) {
        return axios.post(`${API_URL}/login`, { username, password });
    },
    async register(username, password) {
        return axios.post(`${API_URL}/register`, { username, password });
    }
};
