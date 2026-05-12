package com.xiao.xiaopay.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("amount must have at most two decimal places");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
