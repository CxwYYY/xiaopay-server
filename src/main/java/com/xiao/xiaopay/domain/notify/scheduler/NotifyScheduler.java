package com.xiao.xiaopay.domain.notify.scheduler;

import com.xiao.xiaopay.domain.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付回调事件定时调度器。
 */
@Component
@RequiredArgsConstructor
public class NotifyScheduler {
    private final NotifyService notifyService;

    /**
     * 周期性消费支付事件并发起业务系统回调。
     */
    @Scheduled(fixedDelayString = "${xiaopay.notify.fixed-delay-ms:10000}")
    public void processNotifyEvents() {
        notifyService.processPendingEvents();
    }
}
