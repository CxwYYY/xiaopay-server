package com.xiao.xiaopay.domain.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalOrders,
        long pendingOrders,
        long paidOrders,
        long closedOrders,
        long abnormalOrders,
        long todayOrders,
        long todayPaidOrders,
        long todayAbnormalOrders,
        BigDecimal totalPaidAmount,
        BigDecimal todayPaidAmount,
        BigDecimal todayUnmatchedAmount,
        long unmatchedMessages,
        long retryingNotifies,
        long onlineAgents,
        List<AppIncomeRankItem> appIncomeRanking
) {
}
