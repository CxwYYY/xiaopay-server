package com.xiao.xiaopay.common.error;

/**
 * 可预期的业务异常。
 *
 * <p>异常中的 code 会原样映射到接口响应，避免把业务失败当成系统 500。</p>
 */
public class BusinessException extends RuntimeException {
    private final int code;

    /**
     * 创建带业务错误码和提示信息的异常。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 返回给前端或调用方的业务错误码。
     */
    public int code() {
        return code;
    }
}
