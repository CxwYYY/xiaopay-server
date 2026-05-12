package com.xiao.xiaopay.domain.matcher.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.collector.model.MatchStatus;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.matcher.entity.XpOrderMatch;
import com.xiao.xiaopay.domain.matcher.mapper.XpOrderMatchMapper;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 到账消息与支付订单匹配服务。
 *
 * <p>自动匹配条件为 {@code channelId + payNum + amount + pending + 未过期}，异常场景交给后台人工处理。</p>
 */
@Service
@RequiredArgsConstructor
public class OrderMatcherService {
    private final XpWechatMessageMapper messageMapper;
    private final XpPayOrderMapper orderMapper;
    private final XpOrderMatchMapper matchMapper;
    private final PayEventService payEventService;
    private final TimeProvider timeProvider;

    /**
     * 尝试自动匹配单条微信到账消息。
     */
    @Transactional
    public void match(Long wechatMessageId) {
        XpWechatMessage message = messageMapper.selectById(wechatMessageId);
        if (message == null || !MatchStatus.UNMATCHED.name().equals(message.getMatchStatus())) {
            return;
        }
        if (message.getPayNum() == null || message.getPayNum().isBlank()) {
            // 用户付款未填写备注时无法自动确认订单，保留为未匹配到账供后台人工核对。
            return;
        }

        LocalDateTime now = timeProvider.now();
        List<XpPayOrder> exact = orderMapper.selectList(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getChannelId, message.getChannelId())
                .eq(XpPayOrder::getPayNum, message.getPayNum())
                .eq(XpPayOrder::getAmount, message.getAmount())
                .eq(XpPayOrder::getOrderStatus, OrderStatus.PENDING.name())
                .ge(XpPayOrder::getExpireAt, now));
        if (exact.size() == 1) {
            markMatched(message, exact.get(0));
            return;
        }
        if (exact.size() > 1) {
            // 理论上 payNum 生成会避免冲突；如果历史数据或并发导致多单命中，必须人工确认。
            updateMessageStatus(message, MatchStatus.MANUAL, null);
            return;
        }

        Long samePayNumCount = orderMapper.selectCount(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getChannelId, message.getChannelId())
                .eq(XpPayOrder::getPayNum, message.getPayNum())
                .eq(XpPayOrder::getOrderStatus, OrderStatus.PENDING.name())
                .ge(XpPayOrder::getExpireAt, now));
        if (samePayNumCount != null && samePayNumCount > 0) {
            // 备注码存在但金额不一致，通常是用户付款金额填错。
            updateMessageStatus(message, MatchStatus.AMOUNT_MISMATCH, null);
        }
    }

    /**
     * 后台人工把到账消息绑定到待支付订单。
     */
    @Transactional
    public void manualMatch(String orderNo, Long wechatMessageId) {
        XpWechatMessage message = messageMapper.selectById(wechatMessageId);
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>().eq(XpPayOrder::getOrderNo, orderNo));
        if (message == null || order == null) {
            throw new BusinessException(404, "order or message not found");
        }
        if (!OrderStatus.PENDING.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only pending order can be manually matched");
        }
        markMatched(message, order, "MANUAL", "manual matched by admin");
    }

    /**
     * 后台人工解除已支付订单和到账消息的绑定关系。
     */
    @Transactional
    public void unbindMatch(String orderNo, Long wechatMessageId, String reason) {
        XpWechatMessage message = messageMapper.selectById(wechatMessageId);
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>().eq(XpPayOrder::getOrderNo, orderNo));
        if (message == null || order == null) {
            throw new BusinessException(404, "order or message not found");
        }
        if (!OrderStatus.PAID.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only paid order can be unbound");
        }
        if (!orderNo.equals(message.getMatchedOrderNo())) {
            throw new BusinessException(400, "message is not bound to this order");
        }

        LocalDateTime now = timeProvider.now();
        order.setOrderStatus(OrderStatus.PENDING.name());
        order.setPaidAt(null);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);

        updateMessageStatus(message, MatchStatus.UNMATCHED, null);

        XpOrderMatch match = new XpOrderMatch();
        match.setOrderNo(orderNo);
        match.setWechatMessageId(wechatMessageId);
        match.setMatchType("UNBIND");
        match.setMatchResult("UNBOUND");
        match.setReason(reason);
        match.setCreatedAt(now);
        matchMapper.insert(match);
    }

    private void markMatched(XpWechatMessage message, XpPayOrder order) {
        markMatched(message, order, "AUTO", "channelId + payNum + amount matched");
    }

    private void markMatched(XpWechatMessage message, XpPayOrder order, String matchType, String reason) {
        LocalDateTime now = timeProvider.now();
        // 订单置为 PAID 后立即写支付事件，回调由 NotifyScheduler 异步消费。
        order.setOrderStatus(OrderStatus.PAID.name());
        order.setPaidAt(message.getPayTime());
        order.setUpdatedAt(now);
        orderMapper.updateById(order);

        updateMessageStatus(message, MatchStatus.MATCHED, order.getOrderNo());

        XpOrderMatch match = new XpOrderMatch();
        match.setOrderNo(order.getOrderNo());
        match.setWechatMessageId(message.getId());
        match.setMatchType(matchType);
        match.setMatchResult("MATCHED");
        match.setReason(reason);
        match.setCreatedAt(now);
        matchMapper.insert(match);

        payEventService.createOrderPaidEvent(order);
    }

    private void updateMessageStatus(XpWechatMessage message, MatchStatus status, String orderNo) {
        message.setMatchStatus(status.name());
        message.setMatchedOrderNo(orderNo);
        message.setUpdatedAt(timeProvider.now());
        messageMapper.updateById(message);
    }
}
