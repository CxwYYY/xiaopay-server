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

/**
 * 接入应用管理服务。
 *
 * <p>负责应用创建、状态维护和 appSecret 重置；密钥只在创建或重置时返回。</p>
 */
@Service
@RequiredArgsConstructor
public class AppService {
    private final XpAppMapper appMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    /**
     * 创建接入应用并返回首次可见的 appSecret。
     */
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

    /**
     * 查询应用列表，默认不返回 appSecret。
     */
    public List<AppResponse> list() {
        return appMapper.selectList(null).stream().map(app -> toResponse(app, false)).toList();
    }

    /**
     * 查询单个应用详情。
     */
    public AppResponse detail(String appId) {
        return toResponse(getByAppId(appId), false);
    }

    /**
     * 更新应用基础资料和默认回调地址。
     */
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

    /**
     * 启用、停用或软删除应用。
     */
    public AppResponse setStatus(String appId, String status) {
        XpApp app = getByAppId(appId);
        String before = JSONUtil.toJsonStr(app);
        app.setStatus(status);
        app.setUpdatedAt(timeProvider.now());
        appMapper.updateById(app);
        auditLogService.record("UPDATE_APP_STATUS", "APP", appId, before, JSONUtil.toJsonStr(app));
        return toResponse(app, false);
    }

    /**
     * 重置应用签名密钥，并只在本次响应中返回新密钥。
     */
    public AppResponse resetSecret(String appId) {
        XpApp app = getByAppId(appId);
        String before = JSONUtil.toJsonStr(app);
        app.setAppSecret(idGenerator.secret());
        app.setUpdatedAt(timeProvider.now());
        appMapper.updateById(app);
        auditLogService.record("RESET_APP_SECRET", "APP", appId, before, null);
        return toResponse(app, true);
    }

    /**
     * 按 appId 查询应用，不存在时抛出业务异常。
     */
    public XpApp getByAppId(String appId) {
        XpApp app = appMapper.selectOne(new LambdaQueryWrapper<XpApp>().eq(XpApp::getAppId, appId));
        if (app == null) {
            throw new BusinessException(404, "app not found");
        }
        return app;
    }

    /**
     * 转换为接口响应对象，可按场景控制是否包含 appSecret。
     */
    public AppResponse toResponse(XpApp app, boolean includeSecret) {
        return new AppResponse(app.getAppId(), app.getAppName(), includeSecret ? app.getAppSecret() : null,
                app.getStatus(), app.getNotifyUrl(), app.getRemark());
    }
}
