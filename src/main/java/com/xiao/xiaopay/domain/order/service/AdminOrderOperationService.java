package com.xiao.xiaopay.domain.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.NotifyStatus;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理后台订单人工处理服务。
 */
@Service
@RequiredArgsConstructor
public class AdminOrderOperationService {
    private final XpPayOrderMapper orderMapper;
    private final PayEventService payEventService;
    private final AuditLogService auditLogService;
    private final TimeProvider timeProvider;

    /**
     * 人工关闭待支付订单。
     */
    @Transactional
    public void close(String orderNo, String reason) {
        XpPayOrder order = getByOrderNo(orderNo);
        if (!OrderStatus.PENDING.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only pending order can be closed");
        }
        order.setOrderStatus(OrderStatus.CLOSED.name());
        order.setUpdatedAt(timeProvider.now());
        orderMapper.updateById(order);
        auditLogService.record("CLOSE_ORDER", "ORDER", orderNo, null, reason);
    }

    /**
     * 将未支付成功的订单标记为异常。
     */
    @Transactional
    public void markAbnormal(String orderNo, String reason) {
        XpPayOrder order = getByOrderNo(orderNo);
        if (OrderStatus.PAID.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "paid order can not be marked abnormal");
        }
        order.setOrderStatus(OrderStatus.ABNORMAL.name());
        order.setUpdatedAt(timeProvider.now());
        orderMapper.updateById(order);
        auditLogService.record("MARK_ORDER_ABNORMAL", "ORDER", orderNo, null, reason);
    }

    /**
     * 延长待支付订单过期时间。
     */
    @Transactional
    public void extendExpire(String orderNo, int minutes, String reason) {
        XpPayOrder order = getByOrderNo(orderNo);
        if (!OrderStatus.PENDING.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only pending order can extend expire time");
        }
        LocalDateTime now = timeProvider.now();
        LocalDateTime base = order.getExpireAt() == null || order.getExpireAt().isBefore(now)
                ? now
                : order.getExpireAt();
        order.setExpireAt(base.plusMinutes(minutes));
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        auditLogService.record("EXTEND_ORDER_EXPIRE", "ORDER", orderNo, null, reason);
    }

    /**
     * 为已支付订单重新创建支付成功事件，驱动回调重试。
     */
    @Transactional
    public void retryNotify(String orderNo, String reason) {
        XpPayOrder order = getByOrderNo(orderNo);
        if (!OrderStatus.PAID.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only paid order can retry notify");
        }
        order.setNotifyStatus(NotifyStatus.RETRYING.name());
        order.setUpdatedAt(timeProvider.now());
        orderMapper.updateById(order);
        payEventService.createOrderPaidEvent(order);
        auditLogService.record("RETRY_ORDER_NOTIFY", "ORDER", orderNo, null, reason);
    }

    private XpPayOrder getByOrderNo(String orderNo) {
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        return order;
    }
}
