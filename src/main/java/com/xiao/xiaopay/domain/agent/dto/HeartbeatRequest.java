package com.xiao.xiaopay.domain.agent.dto;

/**
 * Agent 心跳上报请求。
 */
public record HeartbeatRequest(
        String hostName,
        String lastError
) {
}
