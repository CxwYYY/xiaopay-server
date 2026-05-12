package com.xiao.xiaopay.domain.collector.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.service.AdminWechatMessageOperationService;
import com.xiao.xiaopay.domain.collector.service.AdminWechatMessageQueryService;
import com.xiao.xiaopay.domain.order.dto.AdminReasonRequest;
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

/**
 * 管理后台微信到账消息查询和人工处理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/wechat-messages")
public class AdminWechatMessageController {
    private final AdminWechatMessageQueryService messageQueryService;
    private final AdminWechatMessageOperationService messageOperationService;

    @GetMapping
    public ApiResponse<PageResult<XpWechatMessage>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String messageId,
            @RequestParam(required = false) String payNum,
            @RequestParam(required = false) String matchStatus,
            @RequestParam(required = false) String matchedOrderNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt) {
        return ApiResponse.ok(messageQueryService.list(pageNo, pageSize, agentId, channelId, messageId,
                payNum, matchStatus, matchedOrderNo, startAt, endAt));
    }

    @PostMapping("/{messageId}/duplicate")
    public ApiResponse<Void> markDuplicate(@PathVariable Long messageId,
                                           @RequestBody(required = false) AdminReasonRequest request) {
        messageOperationService.markDuplicate(messageId, request == null ? null : request.reason());
        return ApiResponse.ok();
    }
}
