package com.xiao.xiaopay.domain.order.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * 付款备注识别码生成服务。
 *
 * <p>默认生成四位数字，冲突过多时自动升级为五位数字。</p>
 */
@Service
public class PayNumService {
    private static final int MAX_ATTEMPTS = 20;
    private final SecureRandom random = new SecureRandom();
    private final IntSupplier randomFourDigit;

    public PayNumService() {
        this.randomFourDigit = () -> 1000 + random.nextInt(9000);
    }

    PayNumService(IntSupplier randomFourDigit) {
        this.randomFourDigit = randomFourDigit;
    }

    /**
     * 生成在调用方业务窗口内不存在的 payNum。
     */
    public String generate(int length, Predicate<String> exists) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = candidate(length);
            if (!exists.test(candidate)) {
                return candidate;
            }
        }
        if (length == 4) {
            return generate(5, exists);
        }
        throw new IllegalStateException("unable to generate unique payNum");
    }

    private String candidate(int length) {
        if (length == 4) {
            return String.valueOf(randomFourDigit.getAsInt());
        }
        int min = (int) Math.pow(10, length - 1);
        int range = (int) Math.pow(10, length) - min;
        return String.valueOf(min + random.nextInt(range));
    }
}
