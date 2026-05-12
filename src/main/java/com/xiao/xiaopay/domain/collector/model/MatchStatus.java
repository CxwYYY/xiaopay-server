package com.xiao.xiaopay.domain.collector.model;

/**
 * 微信到账消息匹配状态。
 */
public enum MatchStatus {
    /** 未匹配到订单，通常需要等待或人工核对。 */
    UNMATCHED,
    /** 已自动或手动匹配到订单。 */
    MATCHED,
    /** 重复到账消息或重复付款记录。 */
    DUPLICATE,
    /** 付款备注命中但金额不一致。 */
    AMOUNT_MISMATCH,
    /** 后台人工处理过的到账消息。 */
    MANUAL
}
