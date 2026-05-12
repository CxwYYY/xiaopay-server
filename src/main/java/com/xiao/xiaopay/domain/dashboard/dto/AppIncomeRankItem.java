package com.xiao.xiaopay.domain.dashboard.dto;

import java.math.BigDecimal;

/**
 * 应用收入排行项。
 */
public record AppIncomeRankItem(
        String appId,
        BigDecimal paidAmount
) {
}
