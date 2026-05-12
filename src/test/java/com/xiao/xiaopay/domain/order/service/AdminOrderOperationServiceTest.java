package com.xiao.xiaopay.domain.order.service;

import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.NotifyStatus;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderOperationServiceTest {
    @Mock
    private XpPayOrderMapper orderMapper;
    @Mock
    private PayEventService payEventService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private TimeProvider timeProvider;
    @InjectMocks
    private AdminOrderOperationService operationService;

    @Test
    void extendsOnlyPendingOrdersAndWritesAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 11, 22, 40);
        XpPayOrder order = new XpPayOrder();
        order.setOrderNo("XP202605110002");
        order.setOrderStatus(OrderStatus.PENDING.name());
        order.setExpireAt(now.plusMinutes(5));
        when(timeProvider.now()).thenReturn(now);
        when(orderMapper.selectOne(any())).thenReturn(order);

        operationService.extendExpire("XP202605110002", 10, "customer requested");

        ArgumentCaptor<XpPayOrder> captor = ArgumentCaptor.forClass(XpPayOrder.class);
        verify(orderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getExpireAt()).isEqualTo(now.plusMinutes(15));
        verify(auditLogService).record("EXTEND_ORDER_EXPIRE", "ORDER", "XP202605110002",
                null, "customer requested");
    }

    @Test
    void retryNotifyRequiresPaidOrder() {
        XpPayOrder order = new XpPayOrder();
        order.setOrderNo("XP202605110003");
        order.setOrderStatus(OrderStatus.PENDING.name());
        when(orderMapper.selectOne(any())).thenReturn(order);

        assertThatThrownBy(() -> operationService.retryNotify("XP202605110003", "manual retry"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("only paid order");
    }

    @Test
    void retryNotifyCreatesNewPaidEventAndMarksRetrying() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 11, 22, 50);
        XpPayOrder order = new XpPayOrder();
        order.setOrderNo("XP202605110004");
        order.setOrderStatus(OrderStatus.PAID.name());
        when(timeProvider.now()).thenReturn(now);
        when(orderMapper.selectOne(any())).thenReturn(order);

        operationService.retryNotify("XP202605110004", "manual retry");

        verify(payEventService).createOrderPaidEvent(order);
        ArgumentCaptor<XpPayOrder> captor = ArgumentCaptor.forClass(XpPayOrder.class);
        verify(orderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getNotifyStatus()).isEqualTo(NotifyStatus.RETRYING.name());
        assertThat(captor.getValue().getUpdatedAt()).isEqualTo(now);
    }
}
