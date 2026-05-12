package com.xiao.xiaopay.domain.event.model;

/**
 * 支付事件类型。
 */
public enum PayEventType {
    /** 订单已支付，用于驱动业务回调。 */
    PAY_ORDER_PAID,
    /** 订单已过期。 */
    PAY_ORDER_EXPIRED,
    /** 业务回调成功。 */
    PAY_NOTIFY_SUCCESS,
    /** 业务回调失败。 */
    PAY_NOTIFY_FAILED
}
