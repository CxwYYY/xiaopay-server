package com.xiao.xiaopay.domain.order.model;

/**
 * 支付订单状态。
 */
public enum OrderStatus {
    /** 待支付，等待微信到账匹配。 */
    PENDING,
    /** 已支付，到账消息已成功匹配订单。 */
    PAID,
    /** 已过期，超过支付有效期仍未完成。 */
    EXPIRED,
    /** 已关闭，由业务方或后台人工关闭。 */
    CLOSED,
    /** 异常订单，需要后台人工核对。 */
    ABNORMAL
}
