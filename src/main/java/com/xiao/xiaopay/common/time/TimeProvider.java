package com.xiao.xiaopay.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 统一时间来源。
 *
 * <p>业务代码通过本组件取当前时间，测试可以注入固定 {@link Clock}。</p>
 */
@Component
public class TimeProvider {
    private final Clock clock;

    /**
     * 使用系统默认时区创建时间提供器。
     */
    public TimeProvider() {
        this(Clock.systemDefaultZone());
    }

    /**
     * 使用指定时钟创建时间提供器，主要用于单元测试。
     */
    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    /**
     * 返回当前本地时间。
     */
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
