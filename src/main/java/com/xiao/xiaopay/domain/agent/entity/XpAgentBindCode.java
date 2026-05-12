package com.xiao.xiaopay.domain.agent.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * One-time code used by xiaopay-agent to claim server-issued credentials.
 */
@Data
@TableName("xp_agent_bind_code")
public class XpAgentBindCode {
    /** Primary key. */
    private Long id;
    /** Human-entered one-time binding code. */
    private String bindCode;
    /** Agent display name to create after a successful claim. */
    private String agentName;
    /** Payment channel bound to the created agent. */
    private Long channelId;
    /** Optional WeChat account label. */
    private String wechatAccount;
    /** Optional host name captured when the admin creates the code. */
    private String hostName;
    /** PENDING, CLAIMED, EXPIRED or CANCELED. */
    private String status;
    /** Agent id created by the successful claim. */
    private String claimedAgentId;
    /** Expiration time. */
    private LocalDateTime expiresAt;
    /** Claim time. */
    private LocalDateTime claimedAt;
    /** Creation time. */
    private LocalDateTime createdAt;
    /** Last update time. */
    private LocalDateTime updatedAt;
}
