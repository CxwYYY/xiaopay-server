package com.xiao.xiaopay.domain.collector.service;

import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.collector.model.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理后台微信到账消息人工处理服务。
 */
@Service
@RequiredArgsConstructor
public class AdminWechatMessageOperationService {
    private final XpWechatMessageMapper messageMapper;
    private final AuditLogService auditLogService;
    private final TimeProvider timeProvider;

    /**
     * 将未匹配或待人工处理的到账消息标记为重复。
     */
    @Transactional
    public void markDuplicate(Long messageId, String reason) {
        XpWechatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "wechat message not found");
        }
        if (MatchStatus.MATCHED.name().equals(message.getMatchStatus())) {
            throw new BusinessException(400, "matched message can not be marked duplicate");
        }
        message.setMatchStatus(MatchStatus.DUPLICATE.name());
        message.setUpdatedAt(timeProvider.now());
        messageMapper.updateById(message);
        auditLogService.record("MARK_WECHAT_MESSAGE_DUPLICATE", "WECHAT_MESSAGE",
                String.valueOf(messageId), null, reason);
    }
}
