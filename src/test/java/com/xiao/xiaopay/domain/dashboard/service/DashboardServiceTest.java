package com.xiao.xiaopay.domain.dashboard.service;

import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.dashboard.dto.DashboardSummaryResponse;
import com.xiao.xiaopay.domain.notify.mapper.XpNotifyRecordMapper;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private XpPayOrderMapper orderMapper;
    @Mock
    private XpWechatMessageMapper messageMapper;
    @Mock
    private XpNotifyRecordMapper notifyRecordMapper;
    @Mock
    private XpAgentMapper agentMapper;
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void returnsOrderMessageNotifyAndAgentSummary() {
        when(orderMapper.selectCount(any()))
                .thenReturn(12L, 3L, 7L, 1L, 1L, 5L, 4L, 1L);
        when(orderMapper.selectObjs(any()))
                .thenReturn(List.of(new BigDecimal("88.50")), List.of(new BigDecimal("22.00")));
        when(orderMapper.selectMaps(any()))
                .thenReturn(List.of(Map.of("app_id", "APP001", "paid_amount", new BigDecimal("22.00"))));
        when(messageMapper.selectObjs(any())).thenReturn(List.of(new BigDecimal("9.90")));
        when(messageMapper.selectCount(any())).thenReturn(2L);
        when(notifyRecordMapper.selectCount(any())).thenReturn(6L);
        when(agentMapper.selectCount(any())).thenReturn(1L);

        DashboardSummaryResponse summary = dashboardService.summary();

        assertThat(summary.totalOrders()).isEqualTo(12L);
        assertThat(summary.pendingOrders()).isEqualTo(3L);
        assertThat(summary.paidOrders()).isEqualTo(7L);
        assertThat(summary.todayOrders()).isEqualTo(5L);
        assertThat(summary.todayPaidOrders()).isEqualTo(4L);
        assertThat(summary.todayAbnormalOrders()).isEqualTo(1L);
        assertThat(summary.totalPaidAmount()).isEqualByComparingTo("88.50");
        assertThat(summary.todayPaidAmount()).isEqualByComparingTo("22.00");
        assertThat(summary.todayUnmatchedAmount()).isEqualByComparingTo("9.90");
        assertThat(summary.unmatchedMessages()).isEqualTo(2L);
        assertThat(summary.retryingNotifies()).isEqualTo(6L);
        assertThat(summary.onlineAgents()).isEqualTo(1L);
        assertThat(summary.appIncomeRanking()).hasSize(1);
        assertThat(summary.appIncomeRanking().getFirst().appId()).isEqualTo("APP001");
    }
}
