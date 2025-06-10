package com.fudan.xiaozhong_dianping.customer.config;

import com.fudan.xiaozhong_dianping.customer.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**") // 需要验证登录状态的接口路径
                .excludePathPatterns("/login") // 排除登录接口
                .excludePathPatterns("/api/groupbuy/packages") // 排除团购套餐查询接口，使其公开可访问
                .excludePathPatterns("/api/groupbuy/packages/**") // 排除团购套餐详情接口，使其公开可访问
                .excludePathPatterns("/api/orders") 
                .excludePathPatterns("/api/orders/**");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /images/** 到项目静态资源目录
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
