package com.xiao.xiaopay.domain.notify.scheduler;

import com.xiao.xiaopay.domain.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifyScheduler {
    private final NotifyService notifyService;

    @Scheduled(fixedDelayString = "${xiaopay.notify.fixed-delay-ms:10000}")
    public void processNotifyEvents() {
        notifyService.processPendingEvents();
    }
}
