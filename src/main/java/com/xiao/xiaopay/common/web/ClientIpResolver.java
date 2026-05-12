package com.xiao.xiaopay.common.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 客户端 IP 解析工具。
 */
public final class ClientIpResolver {
    private ClientIpResolver() {
    }

    /**
     * 优先读取常见反向代理头，再回退到 socket remote address。
     */
    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
