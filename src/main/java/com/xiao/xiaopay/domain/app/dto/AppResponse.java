package com.xiao.xiaopay.domain.app.dto;

public record AppResponse(
        String appId,
        String appName,
        String appSecret,
        String status,
        String notifyUrl,
        String remark
) {
}
