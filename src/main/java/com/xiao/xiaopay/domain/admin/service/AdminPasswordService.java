package com.xiao.xiaopay.domain.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AdminPasswordService {
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}
