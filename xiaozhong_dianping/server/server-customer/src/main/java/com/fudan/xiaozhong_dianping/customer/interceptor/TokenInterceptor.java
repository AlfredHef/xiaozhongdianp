package com.fudan.xiaozhong_dianping.customer.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 对预检请求放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 检查userId头，如果存在则放行
        String userId = request.getHeader("userId");
        if (userId != null && !userId.isEmpty()) {
            return true;
        }
        
        // 验证Authorization头
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 验证 Token 是否存在于 Redis 中
            if (redisTemplate.hasKey(token)) {
                return true;
            }
        }
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}