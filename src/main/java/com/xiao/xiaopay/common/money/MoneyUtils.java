package com.xiao.xiaopay.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额规范化工具。
 *
 * <p>所有入库金额统一限制为正数且最多两位小数，避免浮点和尾差问题。</p>
 */
public final class MoneyUtils {
    private MoneyUtils() {
    }

    /**
     * 校验并格式化金额为 {@code DECIMAL(18,2)} 语义。
     */
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
