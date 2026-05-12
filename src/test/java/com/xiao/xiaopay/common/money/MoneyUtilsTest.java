package com.xiao.xiaopay.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyUtilsTest {
    @Test
    void normalizesMoneyToTwoDecimalPlaces() {
        assertThat(MoneyUtils.normalize(new BigDecimal("19.9"))).isEqualByComparingTo("19.90");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> MoneyUtils.normalize(new BigDecimal("19.999")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("two decimal places");
    }
}
