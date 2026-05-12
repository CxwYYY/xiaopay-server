package com.xiao.xiaopay.common.id;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class IdGenerator {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public long nextId() {
        long timestamp = System.currentTimeMillis();
        long random = UUID.randomUUID().getMostSignificantBits() & 0xFFFFFL;
        return (timestamp << 20) | random;
    }

    public String orderNo() {
        return "XP" + DATE.format(LocalDate.now()) + random(12);
    }

    public String eventId() {
        return "EVT" + random(24);
    }

    public String notifyEventId() {
        return "NE" + random(24);
    }

    public String appId() {
        return "APP" + random(16);
    }

    public String agentId() {
        return "AGT" + random(16);
    }

    public String secret() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String random(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length).toUpperCase();
    }
}
