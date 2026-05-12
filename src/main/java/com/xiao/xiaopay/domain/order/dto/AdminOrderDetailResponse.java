package com.xiao.xiaopay.domain.order.dto;

import com.xiao.xiaopay.domain.audit.entity.XpAuditLog;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.event.entity.XpPayEvent;
import com.xiao.xiaopay.domain.matcher.entity.XpOrderMatch;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;

import java.util.List;

public record AdminOrderDetailResponse(
        XpPayOrder order,
        List<XpWechatMessage> wechatMessages,
        List<XpOrderMatch> matches,
        List<XpPayEvent> events,
        List<XpNotifyRecord> notifyRecords,
        List<XpAuditLog> auditLogs
) {
}
