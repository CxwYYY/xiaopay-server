package com.xiao.xiaopay.domain.app.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.app.dto.AppResponse;
import com.xiao.xiaopay.domain.app.dto.CreateAppRequest;
import com.xiao.xiaopay.domain.app.dto.UpdateAppRequest;
import com.xiao.xiaopay.domain.app.entity.XpApp;
import com.xiao.xiaopay.domain.app.mapper.XpAppMapper;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.common.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppService {
    private final XpAppMapper appMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    public AppResponse create(CreateAppRequest request) {
        LocalDateTime now = timeProvider.now();
        XpApp app = new XpApp();
        app.setAppId(idGenerator.appId());
        app.setAppSecret(idGenerator.secret());
        app.setAppName(request.appName());
        app.setNotifyUrl(request.notifyUrl());
        app.setRemark(request.remark());
        app.setStatus(Status.ENABLED);
        app.setCreatedAt(now);
        app.setUpdatedAt(now);
        appMapper.insert(app);
        auditLogService.record("CREATE_APP", "APP", app.getAppId(), null, JSONUtil.toJsonStr(app));
        return toResponse(app, true);
    }

    public List<AppResponse> list() {
        return appMapper.selectList(null).stream().map(app -> toResponse(app, false)).toList();
    }

    public AppResponse detail(String appId) {
        return toResponse(getByAppId(appId), false);
    }

    public AppResponse update(String appId, UpdateAppRequest request) {
        XpApp app = getByAppId(appId);
        String before = JSONUtil.toJsonStr(app);
        if (request.appName() != null && !request.appName().isBlank()) {
            app.setAppName(request.appName());
        }
        app.setNotifyUrl(request.notifyUrl());
        app.setRemark(request.remark());
        app.setUpdatedAt(timeProvider.now());
        appMapper.updateById(app);
        auditLogService.record("UPDATE_APP", "APP", appId, before, JSONUtil.toJsonStr(app));
        return toResponse(app, false);
    }

    public AppResponse setStatus(String appId, String status) {
        XpApp app = getByAppId(appId);
        String before = JSONUtil.toJsonStr(app);
        app.setStatus(status);
        app.setUpdatedAt(timeProvider.now());
        appMapper.updateById(app);
        auditLogService.record("UPDATE_APP_STATUS", "APP", appId, before, JSONUtil.toJsonStr(app));
        return toResponse(app, false);
    }

    public AppResponse resetSecret(String appId) {
        XpApp app = getByAppId(appId);
        String before = JSONUtil.toJsonStr(app);
        app.setAppSecret(idGenerator.secret());
        app.setUpdatedAt(timeProvider.now());
        appMapper.updateById(app);
        auditLogService.record("RESET_APP_SECRET", "APP", appId, before, null);
        return toResponse(app, true);
    }

    public XpApp getByAppId(String appId) {
        XpApp app = appMapper.selectOne(new LambdaQueryWrapper<XpApp>().eq(XpApp::getAppId, appId));
        if (app == null) {
            throw new BusinessException(404, "app not found");
        }
        return app;
    }

    public AppResponse toResponse(XpApp app, boolean includeSecret) {
        return new AppResponse(app.getAppId(), app.getAppName(), includeSecret ? app.getAppSecret() : null,
                app.getStatus(), app.getNotifyUrl(), app.getRemark());
    }
}
