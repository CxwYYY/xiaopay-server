package com.xiao.xiaopay.domain.app.dto;

/**
 * 更新接入应用请求。
 */
public record UpdateAppRequest(
        String appName,
        String notifyUrl,
        String remark
) {
}
