package com.xiao.xiaopay.common.api;

/**
 * HTTP 接口统一响应包装。
 *
 * <p>业务层只返回数据或抛出 {@code BusinessException}，Controller 统一用本结构输出 code/message/data。</p>
 */
public record ApiResponse<T>(int code, String message, T data) {
    /**
     * 返回带业务数据的成功响应。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 返回无业务数据的成功响应。
     */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    /**
     * 返回已知业务错误响应。
     */
    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
