package com.xiao.xiaopay.domain.agent.dto;

public record HeartbeatRequest(
        String hostName,
        String lastError
) {
}
