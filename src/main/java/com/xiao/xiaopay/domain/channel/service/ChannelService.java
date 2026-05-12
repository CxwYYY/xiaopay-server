package com.xiao.xiaopay.domain.channel.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.channel.dto.ChannelResponse;
import com.xiao.xiaopay.domain.channel.dto.CreateChannelRequest;
import com.xiao.xiaopay.domain.channel.dto.UpdateChannelRequest;
import com.xiao.xiaopay.domain.channel.entity.XpChannel;
import com.xiao.xiaopay.domain.channel.mapper.XpChannelMapper;
import com.xiao.xiaopay.domain.common.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {
    private final XpChannelMapper channelMapper;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    public ChannelResponse create(CreateChannelRequest request) {
        LocalDateTime now = timeProvider.now();
        XpChannel channel = new XpChannel();
        channel.setChannelCode(request.channelCode());
        channel.setChannelName(request.channelName());
        channel.setChannelType(request.channelType());
        channel.setCollectorType(request.collectorType());
        channel.setAgentId(request.agentId());
        channel.setQrCodeUrl(request.qrCodeUrl());
        channel.setReceiverName(request.receiverName());
        channel.setConfigJson(request.configJson());
        channel.setStatus(Status.ENABLED);
        channel.setCreatedAt(now);
        channel.setUpdatedAt(now);
        channelMapper.insert(channel);
        auditLogService.record("CREATE_CHANNEL", "CHANNEL", String.valueOf(channel.getId()), null, JSONUtil.toJsonStr(channel));
        return toResponse(channel);
    }

    public XpChannel selectEnabledWechatChannel() {
        XpChannel channel = channelMapper.selectOne(new LambdaQueryWrapper<XpChannel>()
                .eq(XpChannel::getChannelType, "wechat")
                .eq(XpChannel::getStatus, Status.ENABLED)
                .orderByDesc(XpChannel::getCreatedAt)
                .last("limit 1"));
        if (channel == null) {
            throw new BusinessException(404, "enabled wechat channel not found");
        }
        return channel;
    }

    public XpChannel getById(Long channelId) {
        XpChannel channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BusinessException(404, "channel not found");
        }
        return channel;
    }

    public List<ChannelResponse> list() {
        return channelMapper.selectList(null).stream().map(this::toResponse).toList();
    }

    public ChannelResponse detail(Long channelId) {
        return toResponse(getById(channelId));
    }

    public ChannelResponse update(Long channelId, UpdateChannelRequest request) {
        XpChannel channel = getById(channelId);
        String before = JSONUtil.toJsonStr(channel);
        if (request.channelName() != null && !request.channelName().isBlank()) {
            channel.setChannelName(request.channelName());
        }
        if (request.channelType() != null && !request.channelType().isBlank()) {
            channel.setChannelType(request.channelType());
        }
        if (request.collectorType() != null && !request.collectorType().isBlank()) {
            channel.setCollectorType(request.collectorType());
        }
        channel.setAgentId(request.agentId());
        if (request.qrCodeUrl() != null && !request.qrCodeUrl().isBlank()) {
            channel.setQrCodeUrl(request.qrCodeUrl());
        }
        channel.setReceiverName(request.receiverName());
        channel.setConfigJson(request.configJson());
        channel.setUpdatedAt(timeProvider.now());
        channelMapper.updateById(channel);
        auditLogService.record("UPDATE_CHANNEL", "CHANNEL", String.valueOf(channelId), before, JSONUtil.toJsonStr(channel));
        return toResponse(channel);
    }

    public ChannelResponse setStatus(Long channelId, String status) {
        XpChannel channel = getById(channelId);
        String before = JSONUtil.toJsonStr(channel);
        channel.setStatus(status);
        channel.setUpdatedAt(timeProvider.now());
        channelMapper.updateById(channel);
        auditLogService.record("UPDATE_CHANNEL_STATUS", "CHANNEL", String.valueOf(channelId), before, JSONUtil.toJsonStr(channel));
        return toResponse(channel);
    }

    public ChannelResponse toResponse(XpChannel channel) {
        return new ChannelResponse(channel.getId(), channel.getChannelCode(), channel.getChannelName(),
                channel.getChannelType(), channel.getCollectorType(), channel.getAgentId(),
                channel.getQrCodeUrl(), channel.getReceiverName(), channel.getStatus());
    }
}
