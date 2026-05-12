package com.xiao.xiaopay.domain.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.admin.dto.AdminInitRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginResponse;
import com.xiao.xiaopay.domain.admin.dto.AdminUserResponse;
import com.xiao.xiaopay.domain.admin.entity.XpAdminUser;
import com.xiao.xiaopay.domain.admin.mapper.XpAdminUserMapper;
import com.xiao.xiaopay.domain.common.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final XpAdminUserMapper adminUserMapper;
    private final AdminPasswordService passwordService;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public boolean initialized() {
        Long count = adminUserMapper.selectCount(null);
        return count != null && count > 0;
    }

    @Transactional
    public AdminUserResponse init(AdminInitRequest request) {
        if (initialized()) {
            throw new BusinessException(409, "admin already initialized");
        }
        LocalDateTime now = timeProvider.now();
        XpAdminUser user = new XpAdminUser();
        user.setId(idGenerator.nextId());
        user.setUsername(request.username());
        user.setPasswordHash(passwordService.hash(request.password()));
        user.setNickname(request.nickname());
        user.setStatus(Status.ENABLED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        adminUserMapper.insert(user);
        return toResponse(user);
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        XpAdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<XpAdminUser>()
                .eq(XpAdminUser::getUsername, request.username()));
        if (user == null || !passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(401, "username or password is invalid");
        }
        if (!Status.ENABLED.equals(user.getStatus())) {
            throw new BusinessException(403, "admin user is disabled");
        }
        LocalDateTime now = timeProvider.now();
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        adminUserMapper.updateById(user);

        StpUtil.login(user.getId());
        return new AdminLoginResponse(StpUtil.getTokenName(), StpUtil.getTokenValue(), toResponse(user));
    }

    public AdminUserResponse current() {
        Long userId = StpUtil.getLoginIdAsLong();
        XpAdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "admin user not found");
        }
        return toResponse(user);
    }

    public void logout() {
        StpUtil.logout();
    }

    private AdminUserResponse toResponse(XpAdminUser user) {
        return new AdminUserResponse(user.getId(), user.getUsername(), user.getNickname(),
                user.getStatus(), user.getLastLoginAt());
    }
}
