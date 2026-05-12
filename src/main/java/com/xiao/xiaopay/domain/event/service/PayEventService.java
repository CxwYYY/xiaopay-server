package com.xiao.xiaopay.domain.event.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.event.entity.XpPayEvent;
import com.xiao.xiaopay.domain.event.mapper.XpPayEventMapper;
import com.xiao.xiaopay.domain.event.model.PayEventStatus;
import com.xiao.xiaopay.domain.event.model.PayEventType;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付事件服务。
 *
 * <p>用数据库事件表替代 MQ，可靠驱动支付成功和订单过期后的异步通知流程。</p>
 */
@Service
@RequiredArgsConstructor
public class PayEventService {
    private final XpPayEventMapper eventMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    /**
     * 创建订单支付成功事件。
     */
    public XpPayEvent createOrderPaidEvent(XpPayOrder order) {
        return createOrderEvent(order, PayEventType.PAY_ORDER_PAID);
    }

    /**
     * 创建订单过期事件。
     */
    public XpPayEvent createOrderExpiredEvent(XpPayOrder order) {
        return createOrderEvent(order, PayEventType.PAY_ORDER_EXPIRED);
    }

    private XpPayEvent createOrderEvent(XpPayOrder order, PayEventType eventType) {
        LocalDateTime now = timeProvider.now();
        XpPayEvent event = new XpPayEvent();
        event.setEventId(idGenerator.eventId());
        event.setEventType(eventType.name());
        event.setOrderNo(order.getOrderNo());
        event.setAppId(order.getAppId());
        event.setPayloadJson(JSONUtil.toJsonStr(Map.of("orderNo", order.getOrderNo())));
        event.setEventStatus(PayEventStatus.PENDING.name());
        event.setAttemptCount(0);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        eventMapper.insert(event);
        return event;
    }

    /**
     * 查询当前可处理的待处理或到期重试事件。
     */
    public List<XpPayEvent> pendingEvents(LocalDateTime now, int limit) {
        return eventMapper.selectList(new LambdaQueryWrapper<XpPayEvent>()
                .in(XpPayEvent::getEventStatus, PayEventStatus.PENDING.name(), PayEventStatus.RETRYING.name())
                .and(wrapper -> wrapper.isNull(XpPayEvent::getNextRetryAt).or().le(XpPayEvent::getNextRetryAt, now))
                .orderByAsc(XpPayEvent::getCreatedAt)
                .last("limit " + limit));
    }

    /**
     * 更新时间戳并保存事件状态。
     */
    public void update(XpPayEvent event) {
        event.setUpdatedAt(timeProvider.now());
        eventMapper.updateById(event);
    }
}
