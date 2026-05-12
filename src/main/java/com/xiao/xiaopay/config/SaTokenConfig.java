package com.xiao.xiaopay.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.xiao.xiaopay.common.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理后台登录态和跨域配置。
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {
    private final SecurityProperties securityProperties;

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

    /**
     * 只允许配置内的前端源访问 API，避免开放式跨域。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (securityProperties.getAllowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(securityProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("xiaopay-token")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
