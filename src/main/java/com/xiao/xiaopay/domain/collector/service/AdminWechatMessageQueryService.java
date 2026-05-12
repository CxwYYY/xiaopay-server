package com.xiao.xiaopay.domain.collector.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiao.xiaopay.common.api.PageResult;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理后台微信到账消息查询服务。
 */
@Service
@RequiredArgsConstructor
public class AdminWechatMessageQueryService {
    private static final long MAX_PAGE_SIZE = 200L;

    private final XpWechatMessageMapper messageMapper;

    /**
     * 按 Agent、通道、备注码、匹配状态和到账时间分页查询消息。
     */
    public PageResult<XpWechatMessage> list(long pageNo, long pageSize, String agentId, Long channelId,
                                            String messageId, String payNum, String matchStatus,
                                            String matchedOrderNo, LocalDateTime startAt, LocalDateTime endAt) {
        LambdaQueryWrapper<XpWechatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(agentId != null && !agentId.isBlank(), XpWechatMessage::getAgentId, agentId)
                .eq(channelId != null, XpWechatMessage::getChannelId, channelId)
                .eq(messageId != null && !messageId.isBlank(), XpWechatMessage::getMessageId, messageId)
                .eq(payNum != null && !payNum.isBlank(), XpWechatMessage::getPayNum, payNum)
                .eq(matchStatus != null && !matchStatus.isBlank(), XpWechatMessage::getMatchStatus, matchStatus)
                .eq(matchedOrderNo != null && !matchedOrderNo.isBlank(), XpWechatMessage::getMatchedOrderNo, matchedOrderNo)
                .ge(startAt != null, XpWechatMessage::getPayTime, startAt)
                .le(endAt != null, XpWechatMessage::getPayTime, endAt)
                .orderByDesc(XpWechatMessage::getPayTime);
        Page<XpWechatMessage> page = messageMapper.selectPage(new Page<>(safePageNo(pageNo), safePageSize(pageSize)), wrapper);
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
