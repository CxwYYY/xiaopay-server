package com.xiao.xiaopay.domain.collector.service;

import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.collector.model.MatchStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWechatMessageOperationServiceTest {
    @Mock
    private XpWechatMessageMapper messageMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private TimeProvider timeProvider;
    @InjectMocks
    private AdminWechatMessageOperationService operationService;

    @Test
    void marksUnmatchedMessageAsDuplicateAndWritesAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 11, 23, 10);
        XpWechatMessage message = new XpWechatMessage();
        message.setId(1001L);
        message.setMatchStatus(MatchStatus.UNMATCHED.name());
        when(timeProvider.now()).thenReturn(now);
        when(messageMapper.selectById(1001L)).thenReturn(message);

        operationService.markDuplicate(1001L, "duplicate pay");

        ArgumentCaptor<XpWechatMessage> captor = ArgumentCaptor.forClass(XpWechatMessage.class);
        verify(messageMapper).updateById(captor.capture());
        assertThat(captor.getValue().getMatchStatus()).isEqualTo(MatchStatus.DUPLICATE.name());
        assertThat(captor.getValue().getUpdatedAt()).isEqualTo(now);
        verify(auditLogService).record("MARK_WECHAT_MESSAGE_DUPLICATE", "WECHAT_MESSAGE", "1001", null, "duplicate pay");
    }
}
