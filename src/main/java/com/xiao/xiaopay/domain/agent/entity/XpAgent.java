package com.xiao.xiaopay.domain.agent.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Windows 微信到账采集器。
 *
 * <p>agent 运行在用户本机，负责读取微信到账消息并签名推送到服务端。
 * 服务端通过 {@code agentId}/{@code agentSecret} 认证，并把消息限定到绑定通道。</p>
 */
@Data
@TableName("xp_agent")
public class XpAgent {
    /** 主键 ID。 */
    private Long id;
    /** 采集器编号，用于 agent 签名和身份识别。 */
    private String agentId;
    /** 采集器签名密钥密文或哈希，创建/重置时仅明文返回一次。 */
    private String agentSecret;
    /** 采集器名称。 */
    private String agentName;
    /** 绑定的支付通道 ID。 */
    private Long channelId;
    /** 采集器对应的微信账号标识。 */
    private String wechatAccount;
    /** 采集器所在主机名。 */
    private String hostName;
    /** 采集器状态：ONLINE 在线，OFFLINE 离线，DEGRADED 异常，DISABLED 停用，DELETED 软删除。 */
    private String status;
    /** 最后一次心跳时间。 */
    private LocalDateTime lastHeartbeatAt;
    /** 最近一次上报的错误信息。 */
    private String lastError;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
