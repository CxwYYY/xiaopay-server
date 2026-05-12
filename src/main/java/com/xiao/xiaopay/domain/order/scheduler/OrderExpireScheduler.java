package com.xiao.xiaopay.domain.order.scheduler;

import com.xiao.xiaopay.domain.order.service.OrderExpireService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {
    private final OrderExpireService orderExpireService;

    @Scheduled(fixedDelayString = "${xiaopay.order-expire.fixed-delay-ms:10000}")
    public void expireDueOrders() {
        orderExpireService.expireDueOrders();
    }
}
