package com.xiao.xiaopay.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class TimeProvider {
    private final Clock clock;

    public TimeProvider() {
        this(Clock.systemDefaultZone());
    }

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
