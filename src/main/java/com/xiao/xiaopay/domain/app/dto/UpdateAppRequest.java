package com.xiao.xiaopay.domain.app.dto;

public record UpdateAppRequest(
        String appName,
        String notifyUrl,
        String remark
) {
}
