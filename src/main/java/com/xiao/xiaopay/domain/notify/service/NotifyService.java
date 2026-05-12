package com.xiao.xiaopay.domain.notify.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.security.SignatureService;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.app.entity.XpApp;
import com.xiao.xiaopay.domain.app.mapper.XpAppMapper;
import com.xiao.xiaopay.domain.event.entity.XpPayEvent;
import com.xiao.xiaopay.domain.event.model.PayEventStatus;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.notify.dto.PayNotifyPayload;
import com.xiao.xiaopay.domain.notify.entity.XpNotifyRecord;
import com.xiao.xiaopay.domain.notify.mapper.XpNotifyRecordMapper;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.NotifyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotifyService {
    private final PayEventService payEventService;
    private final XpPayOrderMapper orderMapper;
    private final XpAppMapper appMapper;
    private final XpNotifyRecordMapper notifyRecordMapper;
    private final IdGenerator idGenerator;
    private final SignatureService signatureService;
    private final TimeProvider timeProvider;

    public int processPendingEvents() {
        List<XpPayEvent> events = payEventService.pendingEvents(timeProvider.now(), 50);
        int processed = 0;
        for (XpPayEvent event : events) {
            process(event);
            processed++;
        }
        return processed;
    }

    @Transactional
    public void process(XpPayEvent event) {
        event.setEventStatus(PayEventStatus.PROCESSING.name());
        event.setAttemptCount(event.getAttemptCount() + 1);
        payEventService.update(event);

        XpPayOrder order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<XpPayOrder>()
                .eq(XpPayOrder::getOrderNo, event.getOrderNo()));
        XpApp app = appMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<XpApp>()
                .eq(XpApp::getAppId, event.getAppId()));
        if (order == null || app == null) {
            fail(event, order, "order or app not found");
            return;
        }
        String notifyUrl = order.getNotifyUrl() == null || order.getNotifyUrl().isBlank()
                ? app.getNotifyUrl()
                : order.getNotifyUrl();
        if (notifyUrl == null || notifyUrl.isBlank()) {
            order.setNotifyStatus(NotifyStatus.IGNORED.name());
            order.setUpdatedAt(timeProvider.now());
            orderMapper.updateById(order);
            event.setEventStatus(PayEventStatus.SUCCESS.name());
            payEventService.update(event);
            return;
        }

        String notifyEventId = idGenerator.notifyEventId();
        PayNotifyPayload payload = new PayNotifyPayload(notifyEventId, order.getOrderNo(), order.getAppOrderNo(),
                order.getOrderStatus(), order.getAmount(), order.getPayNum(), order.getPaidAt(),
                order.getBusinessType(), order.getBusinessPayload());
        String body = JSONUtil.toJsonStr(payload);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String signature = signatureService.sign(app.getAppSecret(), timestamp, nonce, body);

        XpNotifyRecord record = newNotifyRecord(event, order, notifyUrl, notifyEventId, body);
        try {
            HttpResponse response = HttpRequest.post(notifyUrl)
                    .header("Content-Type", "application/json")
                    .header("X-XiaoPay-App", app.getAppId())
                    .header("X-XiaoPay-Timestamp", timestamp)
                    .header("X-XiaoPay-Nonce", nonce)
                    .header("X-XiaoPay-Signature", signature)
                    .body(body)
                    .timeout(5000)
                    .execute();
            record.setResponseStatus(response.getStatus());
            record.setResponseBody(response.body());
            if (response.isOk()) {
                record.setNotifyStatus(NotifyStatus.SUCCESS.name());
                event.setEventStatus(PayEventStatus.SUCCESS.name());
                order.setNotifyStatus(NotifyStatus.SUCCESS.name());
            } else {
                record.setNotifyStatus(NotifyStatus.RETRYING.name());
                record.setLastError("http status " + response.getStatus());
                retry(event, order, "http status " + response.getStatus());
            }
        } catch (Exception ex) {
            record.setNotifyStatus(NotifyStatus.RETRYING.name());
            record.setLastError(ex.getMessage());
            retry(event, order, ex.getMessage());
        }
        LocalDateTime now = timeProvider.now();
        record.setUpdatedAt(now);
        notifyRecordMapper.insert(record);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        payEventService.update(event);
    }

    private XpNotifyRecord newNotifyRecord(XpPayEvent event, XpPayOrder order, String notifyUrl, String notifyEventId, String body) {
        LocalDateTime now = timeProvider.now();
        XpNotifyRecord record = new XpNotifyRecord();
        record.setAppId(event.getAppId());
        record.setOrderNo(order.getOrderNo());
        record.setNotifyEventId(notifyEventId);
        record.setNotifyUrl(notifyUrl);
        record.setRequestBody(body);
        record.setAttemptCount(event.getAttemptCount());
        record.setNotifyStatus(NotifyStatus.PENDING.name());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        return record;
    }

    private void retry(XpPayEvent event, XpPayOrder order, String error) {
        event.setEventStatus(PayEventStatus.RETRYING.name());
        event.setNextRetryAt(timeProvider.now().plusMinutes(Math.min(event.getAttemptCount(), 10)));
        event.setLastError(error);
        if (order != null) {
            order.setNotifyStatus(NotifyStatus.RETRYING.name());
        }
    }

    private void fail(XpPayEvent event, XpPayOrder order, String error) {
        event.setEventStatus(PayEventStatus.FAILED.name());
        event.setLastError(error);
        if (order != null) {
            order.setNotifyStatus(NotifyStatus.FAILED.name());
            orderMapper.updateById(order);
        }
        payEventService.update(event);
    }
}
