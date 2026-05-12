package com.xiao.xiaopay.domain.dashboard.dto;

import java.math.BigDecimal;

public record AppIncomeRankItem(
        String appId,
        BigDecimal paidAmount
) {
}
