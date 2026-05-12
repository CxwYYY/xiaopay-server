package com.xiao.xiaopay.domain.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.collector.model.MatchStatus;
import com.xiao.xiaopay.domain.dashboard.dto.AppIncomeRankItem;
import com.xiao.xiaopay.domain.dashboard.dto.DashboardSummaryResponse;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.notify.mapper.XpNotifyRecordMapper;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.NotifyStatus;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final XpPayOrderMapper orderMapper;
    private final XpWechatMessageMapper messageMapper;
    private final XpNotifyRecordMapper notifyRecordMapper;
    private final XpAgentMapper agentMapper;

    public DashboardSummaryResponse summary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        return new DashboardSummaryResponse(
                countOrders(null, null, null),
                countOrders(OrderStatus.PENDING.name(), null, null),
                countOrders(OrderStatus.PAID.name(), null, null),
                countOrders(OrderStatus.CLOSED.name(), null, null),
                countOrders(OrderStatus.ABNORMAL.name(), null, null),
                countOrders(null, todayStart, tomorrowStart),
                countOrders(OrderStatus.PAID.name(), todayStart, tomorrowStart),
                countOrders(OrderStatus.ABNORMAL.name(), todayStart, tomorrowStart),
                sumPaidAmount(null, null),
                sumPaidAmount(todayStart, tomorrowStart),
                sumTodayUnmatchedAmount(todayStart, tomorrowStart),
                countUnmatchedMessages(),
                countRetryingNotifies(),
                countOnlineAgents(),
                appIncomeRanking(todayStart, tomorrowStart)
        );
    }

    private long countOrders(String status, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<XpPayOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(XpPayOrder::getOrderStatus, status);
        }
        if (start != null) {
            wrapper.ge(XpPayOrder::getCreatedAt, start);
        }
        if (end != null) {
            wrapper.lt(XpPayOrder::getCreatedAt, end);
        }
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private BigDecimal sumPaidAmount(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<XpPayOrder> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(amount), 0)");
        wrapper.eq("order_status", OrderStatus.PAID.name());
        if (start != null) {
            wrapper.ge("paid_at", start);
        }
        if (end != null) {
            wrapper.lt("paid_at", end);
        }
        List<Object> values = orderMapper.selectObjs(wrapper);
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(values.get(0).toString());
    }

    private BigDecimal sumTodayUnmatchedAmount(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<XpWechatMessage> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(amount), 0)");
        wrapper.in("match_status", MatchStatus.UNMATCHED.name(), MatchStatus.MANUAL.name(),
                MatchStatus.AMOUNT_MISMATCH.name());
        wrapper.ge("pay_time", start);
        wrapper.lt("pay_time", end);
        List<Object> values = messageMapper.selectObjs(wrapper);
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(values.get(0).toString());
    }

    private List<AppIncomeRankItem> appIncomeRanking(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<XpPayOrder> wrapper = new QueryWrapper<>();
        wrapper.select("app_id", "COALESCE(SUM(amount), 0) AS paid_amount");
        wrapper.eq("order_status", OrderStatus.PAID.name());
        wrapper.ge("paid_at", start);
        wrapper.lt("paid_at", end);
        wrapper.groupBy("app_id");
        wrapper.orderByDesc("paid_amount");
        wrapper.last("limit 10");
        List<Map<String, Object>> rows = orderMapper.selectMaps(wrapper);
        List<AppIncomeRankItem> ranking = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object appId = row.get("app_id");
            Object amount = row.get("paid_amount");
            ranking.add(new AppIncomeRankItem(String.valueOf(appId),
                    amount == null ? BigDecimal.ZERO : new BigDecimal(amount.toString())));
        }
        return ranking;
    }

    private long countUnmatchedMessages() {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<XpWechatMessage>()
                .in(XpWechatMessage::getMatchStatus, MatchStatus.UNMATCHED.name(),
                        MatchStatus.MANUAL.name(), MatchStatus.AMOUNT_MISMATCH.name()));
        return count == null ? 0L : count;
    }

    private long countRetryingNotifies() {
        Long count = notifyRecordMapper.selectCount(new LambdaQueryWrapper<XpNotifyRecord>()
                .eq(XpNotifyRecord::getNotifyStatus, NotifyStatus.RETRYING.name()));
        return count == null ? 0L : count;
    }

    private long countOnlineAgents() {
        Long count = agentMapper.selectCount(new LambdaQueryWrapper<XpAgent>()
                .in(XpAgent::getStatus, "ONLINE", "DEGRADED"));
        return count == null ? 0L : count;
    }
}
