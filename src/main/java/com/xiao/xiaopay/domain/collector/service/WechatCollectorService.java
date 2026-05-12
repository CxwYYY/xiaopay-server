package com.xiao.xiaopay.domain.collector.service;

import com.xiao.xiaopay.common.money.MoneyUtils;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.collector.dto.WechatMessageItem;
import com.xiao.xiaopay.domain.collector.dto.WechatMessagePushRequest;
import com.xiao.xiaopay.domain.collector.dto.WechatMessagePushResponse;
import com.xiao.xiaopay.domain.collector.entity.XpWechatMessage;
import com.xiao.xiaopay.domain.collector.mapper.XpWechatMessageMapper;
import com.xiao.xiaopay.domain.collector.model.MatchStatus;
import com.xiao.xiaopay.domain.matcher.service.OrderMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WechatCollectorService {
    private final XpWechatMessageMapper messageMapper;
    private final OrderMatcherService matcherService;
    private final TimeProvider timeProvider;

    @Transactional
    public WechatMessagePushResponse receive(XpAgent agent, WechatMessagePushRequest request) {
        int inserted = 0;
        int duplicates = 0;
        for (WechatMessageItem item : request.messages()) {
            XpWechatMessage message = toEntity(agent, item);
            try {
                messageMapper.insert(message);
                inserted++;
                matcherService.match(message.getId());
            } catch (DuplicateKeyException ex) {
                duplicates++;
            }
        }
        return new WechatMessagePushResponse(request.messages().size(), inserted, duplicates);
    }

    private XpWechatMessage toEntity(XpAgent agent, WechatMessageItem item) {
        LocalDateTime now = timeProvider.now();
        XpWechatMessage message = new XpWechatMessage();
        message.setAgentId(agent.getAgentId());
        message.setChannelId(agent.getChannelId());
        message.setMessageId(item.messageId());
        message.setAmount(MoneyUtils.normalize(item.amount()));
        message.setPayNum(item.payNum());
        message.setRemarkRaw(item.remarkRaw());
        message.setPayTime(item.payTime());
        message.setTitle(item.title());
        message.setDescription(item.description());
        message.setRawContent(item.rawContent());
        message.setMatchStatus(MatchStatus.UNMATCHED.name());
        message.setReceivedAt(now);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        return message;
    }
}
