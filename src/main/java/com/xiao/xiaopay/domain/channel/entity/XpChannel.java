package com.xiao.xiaopay.domain.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付通道。
 *
 * <p>第一版主要承载微信个人收款码通道，同时保留 {@code channelType}
 * 和 {@code collectorType} 以便后续扩展支付宝、官方接口或人工通道。</p>
 */
@Data
@TableName("xp_channel")
public class XpChannel {
    /** 主键 ID。 */
    private Long id;
    /** 支付通道编码，后台可读的唯一标识。 */
    private String channelCode;
    /** 支付通道名称。 */
    private String channelName;
    /** 支付通道类型：wechat 微信，后续可扩展 alipay 等。 */
    private String channelType;
    /** 采集方式：agent 本地采集器，official_api 官方接口，manual 手工。 */
    private String collectorType;
    /** 绑定的采集器编号，第一版一个 agent 绑定一个 channel。 */
    private String agentId;
    /** 收款二维码地址或资源路径。 */
    private String qrCodeUrl;
    /** 收款人展示名称。 */
    private String receiverName;
    /** 通道状态：ENABLED 启用，DISABLED 停用，DELETED 软删除。 */
    private String status;
    /** 通道扩展配置 JSON。 */
    private String configJson;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
