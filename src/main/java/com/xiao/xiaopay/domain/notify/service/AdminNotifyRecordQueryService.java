package com.xiao.xiaopay.domain.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.notify.mapper.XpNotifyRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理后台回调记录查询服务。
 */
@Service
@RequiredArgsConstructor
public class AdminNotifyRecordQueryService {
    private static final long MAX_PAGE_SIZE = 200L;

    private final XpNotifyRecordMapper notifyRecordMapper;

    /**
     * 按应用、订单、回调事件和状态分页查询回调记录。
     */
    public PageResult<XpNotifyRecord> list(long pageNo, long pageSize, String appId, String orderNo,
                                           String notifyEventId, String notifyStatus,
                                           LocalDateTime startAt, LocalDateTime endAt) {
        LambdaQueryWrapper<XpNotifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(appId != null && !appId.isBlank(), XpNotifyRecord::getAppId, appId)
                .eq(orderNo != null && !orderNo.isBlank(), XpNotifyRecord::getOrderNo, orderNo)
                .eq(notifyEventId != null && !notifyEventId.isBlank(), XpNotifyRecord::getNotifyEventId, notifyEventId)
                .eq(notifyStatus != null && !notifyStatus.isBlank(), XpNotifyRecord::getNotifyStatus, notifyStatus)
                .ge(startAt != null, XpNotifyRecord::getCreatedAt, startAt)
                .le(endAt != null, XpNotifyRecord::getCreatedAt, endAt)
                .orderByDesc(XpNotifyRecord::getCreatedAt);
        Page<XpNotifyRecord> page = notifyRecordMapper.selectPage(new Page<>(safePageNo(pageNo), safePageSize(pageSize)), wrapper);
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    private long safePageNo(long pageNo) {
        return pageNo <= 0 ? 1L : pageNo;
    }

    private long safePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 20L;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
