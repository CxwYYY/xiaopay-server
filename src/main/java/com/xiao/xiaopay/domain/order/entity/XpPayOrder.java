package com.xiao.xiaopay.domain.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单。
 *
 * <p>订单是业务系统与 XiaoPay 支付中台之间的核心契约，包含业务订单号、
 * 支付金额、微信付款备注 {@code payNum}、订单状态和回调状态。</p>
 */
@Data
@TableName("xp_pay_order")
public class XpPayOrder {
    /** 主键 ID。 */
    private Long id;
    /** 接入应用编号。 */
    private String appId;
    /** 业务系统订单号，同一 app 下唯一。 */
    private String appOrderNo;
    /** XiaoPay 支付订单号，全局唯一。 */
    private String orderNo;
    /** 本订单使用的支付通道 ID。 */
    private Long channelId;
    /** 支付方式：wechat 微信。 */
    private String payType;
    /** 订单应付金额。 */
    private BigDecimal amount;
    /** 用户付款备注识别码，用于到账匹配。 */
    private String payNum;
    /** payNum 位数，默认 4 位，冲突过多时升级为 5 位。 */
    private Integer payNumLength;
    /** 订单标题。 */
    private String subject;
    /** 订单描述。 */
    private String description;
    /** 业务系统买家 ID。 */
    private String buyerId;
    /** 业务系统买家名称。 */
    private String buyerName;
    /** 订单级支付结果回调地址，优先于应用默认回调。 */
    private String notifyUrl;
    /** 业务前端支付完成后的返回地址。 */
    private String returnUrl;
    /** 业务类型，如 card、vip、content、shop、custom。 */
    private String businessType;
    /** 业务上下文 JSON，XiaoPay 原样保存并在回调中带回。 */
    private String businessPayload;
    /** 订单状态：PENDING 待支付，PAID 已支付，EXPIRED 已过期，CLOSED 已关闭，ABNORMAL 异常。 */
    private String orderStatus;
    /** 回调状态：PENDING 待通知，SUCCESS 成功，FAILED 失败，RETRYING 重试中，IGNORED 无需通知。 */
    private String notifyStatus;
    /** 订单过期时间。 */
    private LocalDateTime expireAt;
    /** 支付成功时间。 */
    private LocalDateTime paidAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
