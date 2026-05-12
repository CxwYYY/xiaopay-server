package com.xiao.xiaopay.domain.audit.service;

import cn.hutool.json.JSONUtil;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.entity.XpAuditLog;
import com.xiao.xiaopay.domain.audit.mapper.XpAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final XpAuditLogMapper auditLogMapper;
    private final TimeProvider timeProvider;

    public void record(String action, String targetType, String targetId, String beforeJson, String afterJson) {
        XpAuditLog log = new XpAuditLog();
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeJson(normalizeJson(beforeJson));
        log.setAfterJson(normalizeJson(afterJson));
        log.setCreatedAt(timeProvider.now());
        auditLogMapper.insert(log);
    }

    private String normalizeJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (JSONUtil.isTypeJSON(value)) {
            return value;
        }
        return JSONUtil.toJsonStr(Map.of("value", value));
    }
}
