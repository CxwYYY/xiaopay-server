package com.xiao.xiaopay.common.error;

import cn.dev33.satoken.exception.NotLoginException;
import com.xiao.xiaopay.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>把业务异常、参数校验异常、唯一键冲突和后台未登录统一转换为 {@link ApiResponse}。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理服务层主动抛出的业务失败。
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.code(), ex.getMessage());
    }

    /**
     * 处理 {@code @RequestBody} 参数校验失败，只返回第一条字段错误。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
        return ApiResponse.error(400, message);
    }

    /**
     * 处理 {@code @RequestParam}/{@code @PathVariable} 参数约束失败。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    /**
     * 处理数据库唯一索引冲突，例如应用订单号或 Agent 消息 ID 重复。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicate(DuplicateKeyException ex) {
        return ApiResponse.error(409, "duplicate record");
    }

    /**
     * 处理管理后台未登录或登录态失效。
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException ex) {
        return ApiResponse.error(401, "admin not login");
    }
}
