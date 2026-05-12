package com.xiao.xiaopay.domain.order.scheduler;

import com.xiao.xiaopay.domain.order.service.OrderExpireService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单过期扫描定时调度器。
 */
@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {
    private final OrderExpireService orderExpireService;

    /**
     * 周期性把超时未支付订单置为过期。
     */
    @Scheduled(fixedDelayString = "${xiaopay.order-expire.fixed-delay-ms:10000}")
    public void expireDueOrders() {
        orderExpireService.expireDueOrders();
    }
}
