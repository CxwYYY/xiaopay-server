package com.xiao.xiaopay.domain.order.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PayNumServiceTest {
    @Test
    void generatesFourDigitPayNumByDefault() {
        PayNumService service = new PayNumService();

        String payNum = service.generate(4, value -> false);

        assertThat(payNum).matches("[1-9][0-9]{3}");
    }

    @Test
    void skipsConflictingPayNums() {
        PayNumService service = new PayNumService(() -> 1234);
        Set<String> conflicts = Set.of("1234");

        String payNum = service.generate(4, conflicts::contains);

        assertThat(payNum).matches("[1-9][0-9]{4}");
    }
}
