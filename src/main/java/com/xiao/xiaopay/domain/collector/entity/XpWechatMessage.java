package com.xiao.xiaopay.domain.collector.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信到账消息。
 *
 * <p>由 {@code xiaopay-agent} 上报，服务端按 {@code agentId + messageId}
 * 幂等入库，再根据通道、金额和付款备注匹配支付订单。</p>
 */
@Data
@TableName("xp_wechat_message")
public class XpWechatMessage {
    /** 主键 ID。 */
    private Long id;
    /** 上报该消息的采集器编号。 */
    private String agentId;
    /** 消息归属的支付通道 ID。 */
    private Long channelId;
    /** 微信消息唯一 ID，同一 agent 下用于幂等。 */
    private String messageId;
    /** 微信到账金额。 */
    private BigDecimal amount;
    /** 从付款备注中解析出的识别码，可能为空。 */
    private String payNum;
    /** 微信付款备注原文。 */
    private String remarkRaw;
    /** 微信到账时间。 */
    private LocalDateTime payTime;
    /** 微信消息标题。 */
    private String title;
    /** 微信消息描述。 */
    private String description;
    /** 原始消息内容或 XML，用于排查和人工核对。 */
    private String rawContent;
    /** 匹配状态：UNMATCHED 未匹配，MATCHED 已匹配，DUPLICATE 重复，AMOUNT_MISMATCH 金额不符，MANUAL 手动处理。 */
    private String matchStatus;
    /** 已匹配的 XiaoPay 订单号。 */
    private String matchedOrderNo;
    /** 服务端接收消息时间。 */
    private LocalDateTime receivedAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
