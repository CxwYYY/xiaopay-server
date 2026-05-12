package com.xiao.xiaopay.domain.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;

/**
 * 管理员密码哈希服务。
 */
@Service
public class AdminPasswordService {
    /**
     * 使用 BCrypt 生成不可逆密码哈希。
     */
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码和 BCrypt 哈希是否匹配。
     */
    public boolean matches(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}
