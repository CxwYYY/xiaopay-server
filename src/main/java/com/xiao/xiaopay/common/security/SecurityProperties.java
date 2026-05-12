package com.xiao.xiaopay.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * XiaoPay 安全相关配置。
 */
@Component
@ConfigurationProperties(prefix = "xiaopay.security")
public class SecurityProperties {
    private long signatureWindowSeconds = 300;
    private String setupToken = "";
    private int loginMaxFailures = 5;
    private int loginLockMinutes = 15;
    private List<String> allowedOrigins = new ArrayList<>();

    public long getSignatureWindowSeconds() {
        return signatureWindowSeconds;
    }

    public void setSignatureWindowSeconds(long signatureWindowSeconds) {
        this.signatureWindowSeconds = signatureWindowSeconds;
    }

    public String getSetupToken() {
        return setupToken;
    }

    public void setSetupToken(String setupToken) {
        this.setupToken = setupToken;
    }

    public int getLoginMaxFailures() {
        return loginMaxFailures;
    }

    public void setLoginMaxFailures(int loginMaxFailures) {
        this.loginMaxFailures = loginMaxFailures;
    }

    public int getLoginLockMinutes() {
        return loginLockMinutes;
    }

    public void setLoginLockMinutes(int loginLockMinutes) {
        this.loginLockMinutes = loginLockMinutes;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
    }
}
