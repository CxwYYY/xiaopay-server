package com.xiao.xiaopay.domain.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务回调记录。
 *
 * <p>支付成功后对业务系统发起 HTTP 通知，本表保存请求、响应、
 * 尝试次数和下次重试时间，便于排查回调失败。</p>
 */
@Data
@TableName("xp_notify_record")
public class XpNotifyRecord {
    /** 主键 ID。 */
    private Long id;
    /** 接入应用编号。 */
    private String appId;
    /** XiaoPay 支付订单号。 */
    private String orderNo;
    /** 通知事件编号，用于业务方幂等处理。 */
    private String notifyEventId;
    /** 实际请求的业务回调地址。 */
    private String notifyUrl;
    /** 回调请求体 JSON。 */
    private String requestBody;
    /** 业务方 HTTP 响应状态码。 */
    private Integer responseStatus;
    /** 业务方 HTTP 响应内容。 */
    private String responseBody;
    /** 回调尝试次数。 */
    private Integer attemptCount;
    /** 下次重试时间。 */
    private LocalDateTime nextRetryAt;
    /** 通知状态：PENDING 待通知，SUCCESS 成功，FAILED 失败，RETRYING 重试中。 */
    private String notifyStatus;
    /** 最近一次回调错误。 */
    private String lastError;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
