package com.xiao.xiaopay.domain.order.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.domain.order.dto.AdminReasonRequest;
import com.xiao.xiaopay.domain.order.dto.AdminOrderDetailResponse;
import com.xiao.xiaopay.domain.order.dto.ExtendExpireRequest;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.service.AdminOrderOperationService;
import com.xiao.xiaopay.domain.order.service.AdminOrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final AdminOrderQueryService orderQueryService;
    private final AdminOrderOperationService orderOperationService;

    @GetMapping
    public ApiResponse<PageResult<XpPayOrder>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String appOrderNo,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String notifyStatus,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt) {
        return ApiResponse.ok(orderQueryService.list(pageNo, pageSize, appId, orderNo, appOrderNo,
                orderStatus, notifyStatus, businessType, startAt, endAt));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<AdminOrderDetailResponse> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderQueryService.detail(orderNo));
    }

    @PostMapping("/{orderNo}/close")
    public ApiResponse<Void> close(@PathVariable String orderNo,
                                   @RequestBody(required = false) AdminReasonRequest request) {
        orderOperationService.close(orderNo, request == null ? null : request.reason());
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/abnormal")
    public ApiResponse<Void> markAbnormal(@PathVariable String orderNo,
                                          @RequestBody(required = false) AdminReasonRequest request) {
        orderOperationService.markAbnormal(orderNo, request == null ? null : request.reason());
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/expire/extend")
    public ApiResponse<Void> extendExpire(@PathVariable String orderNo,
                                          @Valid @RequestBody ExtendExpireRequest request) {
        orderOperationService.extendExpire(orderNo, request.minutes(), request.reason());
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/notify/retry")
    public ApiResponse<Void> retryNotify(@PathVariable String orderNo,
                                         @RequestBody(required = false) AdminReasonRequest request) {
        orderOperationService.retryNotify(orderNo, request == null ? null : request.reason());
        return ApiResponse.ok();
    }
}
