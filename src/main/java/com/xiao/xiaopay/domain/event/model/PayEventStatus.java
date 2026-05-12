package com.xiao.xiaopay.domain.event.model;

/**
 * 支付事件处理状态。
 */
public enum PayEventStatus {
    /** 等待定时任务处理。 */
    PENDING,
    /** 正在处理。 */
    PROCESSING,
    /** 处理成功。 */
    SUCCESS,
    /** 处理失败且当前不再立即重试。 */
    FAILED,
    /** 处理失败后等待下次重试。 */
    RETRYING
}
