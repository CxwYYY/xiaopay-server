package com.xiao.xiaopay.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理后台登录态拦截配置。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    /**
     * 拦截所有后台管理接口，并放行初始化和登录入口。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpUtil.checkLogin()))
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/initialized", "/api/admin/auth/init",
                        "/api/admin/auth/login", "/api/admin/login");
    }
}
