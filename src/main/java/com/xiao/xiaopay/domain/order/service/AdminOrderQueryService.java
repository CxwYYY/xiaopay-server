package com.xiao.xiaopay.domain.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.domain.audit.entity.XpAuditLog;
import com.xiao.xiaopay.domain.audit.mapper.XpAuditLogMapper;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.event.entity.XpPayEvent;
import com.xiao.xiaopay.domain.event.mapper.XpPayEventMapper;
import com.xiao.xiaopay.domain.matcher.entity.XpOrderMatch;
import com.xiao.xiaopay.domain.matcher.mapper.XpOrderMatchMapper;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.notify.mapper.XpNotifyRecordMapper;
import com.xiao.xiaopay.domain.order.dto.AdminOrderDetailResponse;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderQueryService {
    private static final long MAX_PAGE_SIZE = 200L;

    private final XpPayOrderMapper orderMapper;
    private final XpWechatMessageMapper messageMapper;
    private final XpOrderMatchMapper matchMapper;
    private final XpPayEventMapper eventMapper;
    private final XpNotifyRecordMapper notifyRecordMapper;
    private final XpAuditLogMapper auditLogMapper;

    public PageResult<XpPayOrder> list(long pageNo, long pageSize, String appId, String orderNo,
                                       String appOrderNo, String orderStatus, String notifyStatus,
                                       String businessType, LocalDateTime startAt, LocalDateTime endAt) {
        LambdaQueryWrapper<XpPayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(appId != null && !appId.isBlank(), XpPayOrder::getAppId, appId)
                .eq(orderNo != null && !orderNo.isBlank(), XpPayOrder::getOrderNo, orderNo)
                .eq(appOrderNo != null && !appOrderNo.isBlank(), XpPayOrder::getAppOrderNo, appOrderNo)
                .eq(orderStatus != null && !orderStatus.isBlank(), XpPayOrder::getOrderStatus, orderStatus)
                .eq(notifyStatus != null && !notifyStatus.isBlank(), XpPayOrder::getNotifyStatus, notifyStatus)
                .eq(businessType != null && !businessType.isBlank(), XpPayOrder::getBusinessType, businessType)
                .ge(startAt != null, XpPayOrder::getCreatedAt, startAt)
                .le(endAt != null, XpPayOrder::getCreatedAt, endAt)
                .orderByDesc(XpPayOrder::getCreatedAt);
        Page<XpPayOrder> page = orderMapper.selectPage(new Page<>(safePageNo(pageNo), safePageSize(pageSize)), wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    public AdminOrderDetailResponse detail(String orderNo) {
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        List<XpOrderMatch> matches = matchMapper.selectList(new LambdaQueryWrapper<XpOrderMatch>()
                .eq(XpOrderMatch::getOrderNo, orderNo)
                .orderByDesc(XpOrderMatch::getCreatedAt));
        List<XpWechatMessage> messages = selectOrderMessages(orderNo, matches);
        List<XpPayEvent> events = eventMapper.selectList(new LambdaQueryWrapper<XpPayEvent>()
                .eq(XpPayEvent::getOrderNo, orderNo)
                .orderByDesc(XpPayEvent::getCreatedAt));
        List<XpNotifyRecord> records = notifyRecordMapper.selectList(new LambdaQueryWrapper<XpNotifyRecord>()
                .eq(XpNotifyRecord::getOrderNo, orderNo)
                .orderByDesc(XpNotifyRecord::getCreatedAt));
        List<XpAuditLog> audits = auditLogMapper.selectList(new LambdaQueryWrapper<XpAuditLog>()
                .eq(XpAuditLog::getTargetType, "ORDER")
                .eq(XpAuditLog::getTargetId, orderNo)
                .orderByDesc(XpAuditLog::getCreatedAt));
        return new AdminOrderDetailResponse(order, messages, matches, events, records, audits);
    }

    private List<XpWechatMessage> selectOrderMessages(String orderNo, List<XpOrderMatch> matches) {
        LinkedHashMap<Long, XpWechatMessage> messageMap = new LinkedHashMap<>();
        List<XpWechatMessage> matchedByOrderNo = messageMapper.selectList(new LambdaQueryWrapper<XpWechatMessage>()
                .eq(XpWechatMessage::getMatchedOrderNo, orderNo)
                .orderByDesc(XpWechatMessage::getPayTime));
        for (XpWechatMessage message : matchedByOrderNo) {
            messageMap.put(message.getId(), message);
        }
        List<Long> messageIds = new ArrayList<>();
        for (XpOrderMatch match : matches) {
            messageIds.add(match.getWechatMessageId());
        }
        if (!messageIds.isEmpty()) {
            List<XpWechatMessage> matchedById = messageMapper.selectByIds(messageIds);
            for (XpWechatMessage message : matchedById) {
                messageMap.putIfAbsent(message.getId(), message);
            }
        }
        return new ArrayList<>(messageMap.values());
    }

    private long safePageNo(long pageNo) {
        return pageNo <= 0 ? 1L : pageNo;
    }

    private long safePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 20L;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
