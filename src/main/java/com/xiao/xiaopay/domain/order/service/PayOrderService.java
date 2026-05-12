package com.xiao.xiaopay.domain.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.money.MoneyUtils;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.channel.entity.XpChannel;
import com.xiao.xiaopay.domain.channel.service.ChannelService;
import com.xiao.xiaopay.domain.order.dto.CreateOrderRequest;
import com.xiao.xiaopay.domain.order.dto.CreateOrderResponse;
import com.xiao.xiaopay.domain.order.dto.OrderStatusResponse;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.NotifyStatus;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 商户侧支付订单服务。
 *
 * <p>负责签名应用下单、订单查询、商户关闭订单和 payNum 唯一性检查。</p>
 */
@Service
@RequiredArgsConstructor
public class PayOrderService {
    private final XpPayOrderMapper orderMapper;
    private final ChannelService channelService;
    private final PayNumService payNumService;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final StringRedisTemplate redisTemplate;

    /**
     * 创建支付订单。
     *
     * <p>同一 {@code appId + appOrderNo} 重复请求会返回已有订单，避免业务系统重复下单。</p>
     */
    @Transactional
    public CreateOrderResponse create(String appId, CreateOrderRequest request) {
        BigDecimal amount = MoneyUtils.normalize(request.amount());
        XpPayOrder existing = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getAppId, appId)
                .eq(XpPayOrder::getAppOrderNo, request.appOrderNo()));
        if (existing != null) {
            return toCreateResponse(existing, channelService.getById(existing.getChannelId()).getQrCodeUrl());
        }

        XpChannel channel = channelService.selectEnabledWechatChannel();
        String lockKey = "xiaopay:paynum:" + channel.getId() + ":" + amount;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(429, "payNum generation is busy, please retry");
        }
        try {
            LocalDateTime now = timeProvider.now();
            LocalDateTime expireAt = now.plusSeconds(request.expireSeconds() == null ? 900 : request.expireSeconds());
            // 同一通道、同一金额、未过期待支付订单内，payNum 必须唯一，才能靠付款备注精确匹配。
            String payNum = payNumService.generate(4, candidate -> existsPendingPayNum(channel.getId(), amount, candidate, now));

            XpPayOrder order = new XpPayOrder();
            order.setAppId(appId);
            order.setAppOrderNo(request.appOrderNo());
            order.setOrderNo(idGenerator.orderNo());
            order.setChannelId(channel.getId());
            order.setPayType("wechat");
            order.setAmount(amount);
            order.setPayNum(payNum);
            order.setPayNumLength(payNum.length());
            order.setSubject(request.subject());
            order.setDescription(request.description());
            order.setBuyerId(request.buyerId());
            order.setBuyerName(request.buyerName());
            order.setNotifyUrl(request.notifyUrl());
            order.setReturnUrl(request.returnUrl());
            order.setBusinessType(request.businessType());
            order.setBusinessPayload(request.businessPayload());
            order.setOrderStatus(OrderStatus.PENDING.name());
            order.setNotifyStatus(NotifyStatus.PENDING.name());
            order.setExpireAt(expireAt);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            orderMapper.insert(order);
            return toCreateResponse(order, channel.getQrCodeUrl());
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 查询商户自己应用下的订单状态。
     */
    public OrderStatusResponse getOrder(String appId, String orderNo) {
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getAppId, appId)
                .eq(XpPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        return toStatusResponse(order);
    }

    /**
     * 商户主动关闭自己应用下的待支付订单。
     */
    public void close(String appId, String orderNo) {
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getAppId, appId)
                .eq(XpPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        if (!OrderStatus.PENDING.name().equals(order.getOrderStatus())) {
            throw new BusinessException(400, "only pending order can be closed");
        }
        order.setOrderStatus(OrderStatus.CLOSED.name());
        order.setUpdatedAt(timeProvider.now());
        orderMapper.updateById(order);
    }

    /**
     * 按 XiaoPay 订单号查询订单。
     */
    public XpPayOrder getByOrderNo(String orderNo) {
        XpPayOrder order = orderMapper.selectOne(new LambdaQueryWrapper<XpPayOrder>().eq(XpPayOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        return order;
    }

    /**
     * 检查未过期待支付订单中是否已存在相同 payNum。
     */
    public boolean existsPendingPayNum(Long channelId, BigDecimal amount, String payNum, LocalDateTime now) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getChannelId, channelId)
                .eq(XpPayOrder::getAmount, amount)
                .eq(XpPayOrder::getPayNum, payNum)
                .eq(XpPayOrder::getOrderStatus, OrderStatus.PENDING.name())
                .gt(XpPayOrder::getExpireAt, now));
        return count != null && count > 0;
    }

    private CreateOrderResponse toCreateResponse(XpPayOrder order, String qrCodeUrl) {
        return new CreateOrderResponse(order.getOrderNo(), order.getAppOrderNo(), order.getAmount(),
                order.getPayNum(), order.getPayType(), order.getChannelId(), qrCodeUrl,
                order.getOrderStatus(), order.getExpireAt(), order.getCreatedAt());
    }

    private OrderStatusResponse toStatusResponse(XpPayOrder order) {
        return new OrderStatusResponse(order.getOrderNo(), order.getAppOrderNo(), order.getAmount(), order.getPayNum(),
                order.getOrderStatus(), order.getNotifyStatus(), order.getExpireAt(), order.getPaidAt(),
                order.getBusinessType(), order.getBusinessPayload());
    }
}
