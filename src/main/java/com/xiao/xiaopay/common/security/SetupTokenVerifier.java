package com.xiao.xiaopay.common.security;

import com.xiao.xiaopay.common.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 后台初始化令牌校验器。
 */
@Service
@RequiredArgsConstructor
public class SetupTokenVerifier {
    private final SecurityProperties securityProperties;

    /**
     * 校验初始化令牌；未配置令牌时只允许本机初始化。
     */
    public void verify(String providedToken, String remoteAddress) {
        String expectedToken = securityProperties.getSetupToken();
        if (expectedToken != null && !expectedToken.isBlank()) {
            if (!expectedToken.equals(providedToken)) {
                throw new BusinessException(401, "invalid setup token");
            }
            return;
        }
        if (!isLoopback(remoteAddress)) {
            throw new BusinessException(401, "setup token is required");
        }
    }

    private boolean isLoopback(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.isBlank()) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(remoteAddress);
            return address.isLoopbackAddress();
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}
