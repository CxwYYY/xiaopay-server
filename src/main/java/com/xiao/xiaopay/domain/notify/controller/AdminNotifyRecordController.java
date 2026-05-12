package com.xiao.xiaopay.domain.notify.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.notify.service.AdminNotifyRecordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 管理后台回调记录查询接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notify-records")
public class AdminNotifyRecordController {
    private final AdminNotifyRecordQueryService notifyRecordQueryService;

    @GetMapping
    public ApiResponse<PageResult<XpNotifyRecord>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String notifyEventId,
            @RequestParam(required = false) String notifyStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt) {
        return ApiResponse.ok(notifyRecordQueryService.list(pageNo, pageSize, appId, orderNo,
                notifyEventId, notifyStatus, startAt, endAt));
    }
}
