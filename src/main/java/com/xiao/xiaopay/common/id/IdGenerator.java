package com.xiao.xiaopay.common.id;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 业务编号和密钥生成器。
 *
 * <p>负责生成订单号、事件号、应用/Agent 编号和一次性展示的接入密钥。</p>
 */
@Component
public class IdGenerator {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 生成用于后台用户等内部表的长整型 ID。
     */
    public long nextId() {
        long timestamp = System.currentTimeMillis();
        long random = UUID.randomUUID().getMostSignificantBits() & 0xFFFFFL;
        return (timestamp << 20) | random;
    }

    /**
     * 生成对外支付订单号。
     */
    public String orderNo() {
        return "XP" + DATE.format(LocalDate.now()) + random(12);
    }

    /**
     * 生成支付事件 ID。
     */
    public String eventId() {
        return "EVT" + random(24);
    }

    /**
     * 生成回调事件 ID，用于调用业务系统时做幂等追踪。
     */
    public String notifyEventId() {
        return "NE" + random(24);
    }

    /**
     * 生成接入应用编号。
     */
    public String appId() {
        return "APP" + random(16);
    }

    /**
     * 生成采集 Agent 编号。
     */
    public String agentId() {
        return "AGT" + random(16);
    }

    /**
     * 生成应用或 Agent 的 HMAC 签名密钥。
     */
    public String secret() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String random(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length).toUpperCase();
    }
}
