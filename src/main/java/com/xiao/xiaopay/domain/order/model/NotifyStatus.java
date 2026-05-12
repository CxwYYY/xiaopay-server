package com.xiao.xiaopay.domain.order.model;

/**
 * 订单回调状态。
 */
public enum NotifyStatus {
    /** 待通知业务系统。 */
    PENDING,
    /** 已成功通知业务系统。 */
    SUCCESS,
    /** 通知失败且当前无可立即执行的重试。 */
    FAILED,
    /** 通知失败后等待定时任务重试。 */
    RETRYING,
    /** 无回调地址或无需通知。 */
    IGNORED
}
