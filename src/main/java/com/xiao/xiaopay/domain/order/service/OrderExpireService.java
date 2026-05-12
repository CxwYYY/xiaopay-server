package com.xiao.xiaopay.domain.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单过期处理服务。
 */
@Service
@RequiredArgsConstructor
public class OrderExpireService {
    private static final int BATCH_SIZE = 100;

    private final XpPayOrderMapper orderMapper;
    private final PayEventService payEventService;
    private final TimeProvider timeProvider;

    /**
     * 批量把已过期的待支付订单置为 EXPIRED，并生成过期事件。
     */
    @Transactional
    public int expireDueOrders() {
        LocalDateTime now = timeProvider.now();
        List<XpPayOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getOrderStatus, OrderStatus.PENDING.name())
                .le(XpPayOrder::getExpireAt, now)
                .orderByAsc(XpPayOrder::getExpireAt)
                .last("limit " + BATCH_SIZE));
        for (XpPayOrder order : orders) {
            order.setOrderStatus(OrderStatus.EXPIRED.name());
            order.setUpdatedAt(now);
            orderMapper.updateById(order);
            payEventService.createOrderExpiredEvent(order);
        }
        return orders.size();
    }
}
