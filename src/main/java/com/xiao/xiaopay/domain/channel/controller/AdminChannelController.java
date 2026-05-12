package com.xiao.xiaopay.domain.channel.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.channel.dto.ChannelResponse;
import com.xiao.xiaopay.domain.channel.dto.CreateChannelRequest;
import com.xiao.xiaopay.domain.channel.dto.UpdateChannelRequest;
import com.xiao.xiaopay.domain.channel.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/channels")
public class AdminChannelController {
    private final ChannelService channelService;

    @PostMapping
    public ApiResponse<ChannelResponse> create(@Valid @RequestBody CreateChannelRequest request) {
        return ApiResponse.ok(channelService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ChannelResponse>> list() {
        return ApiResponse.ok(channelService.list());
    }

    @GetMapping("/{channelId}")
    public ApiResponse<ChannelResponse> detail(@PathVariable Long channelId) {
        return ApiResponse.ok(channelService.detail(channelId));
    }

    @PutMapping("/{channelId}")
    public ApiResponse<ChannelResponse> update(@PathVariable Long channelId,
                                               @RequestBody UpdateChannelRequest request) {
        return ApiResponse.ok(channelService.update(channelId, request));
    }

    @PostMapping("/{channelId}/enable")
    public ApiResponse<ChannelResponse> enable(@PathVariable Long channelId) {
        return ApiResponse.ok(channelService.setStatus(channelId, "ENABLED"));
    }

    @PostMapping("/{channelId}/disable")
    public ApiResponse<ChannelResponse> disable(@PathVariable Long channelId) {
        return ApiResponse.ok(channelService.setStatus(channelId, "DISABLED"));
    }

    @DeleteMapping("/{channelId}")
    public ApiResponse<ChannelResponse> delete(@PathVariable Long channelId) {
        return ApiResponse.ok(channelService.setStatus(channelId, "DISABLED"));
    }
}
