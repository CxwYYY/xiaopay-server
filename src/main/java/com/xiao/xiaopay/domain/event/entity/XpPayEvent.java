package com.xiao.xiaopay.domain.event.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付事件。
 *
 * <p>第一版不引入 MQ，使用 MySQL 事件表作为可靠事件来源，
 * 由定时任务扫描并驱动业务回调和后续状态推进。</p>
 */
@Data
@TableName("xp_pay_event")
public class XpPayEvent {
    /** 主键 ID。 */
    private Long id;
    /** 支付事件编号，用于幂等。 */
    private String eventId;
    /** 事件类型，如 PAY_ORDER_PAID、PAY_ORDER_EXPIRED、PAY_NOTIFY_SUCCESS、PAY_NOTIFY_FAILED。 */
    private String eventType;
    /** 关联的 XiaoPay 支付订单号。 */
    private String orderNo;
    /** 接入应用编号。 */
    private String appId;
    /** 事件载荷 JSON。 */
    private String payloadJson;
    /** 事件状态：PENDING 待处理，PROCESSING 处理中，SUCCESS 成功，FAILED 失败，RETRYING 重试中。 */
    private String eventStatus;
    /** 事件处理尝试次数。 */
    private Integer attemptCount;
    /** 下次重试时间。 */
    private LocalDateTime nextRetryAt;
    /** 最近一次处理错误。 */
    private String lastError;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
