package com.xiao.xiaopay.domain.order.service;

import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.event.service.PayEventService;
import com.xiao.xiaopay.domain.order.entity.XpPayOrder;
import com.xiao.xiaopay.domain.order.mapper.XpPayOrderMapper;
import com.xiao.xiaopay.domain.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExpireServiceTest {
    @Mock
    private XpPayOrderMapper orderMapper;
    @Mock
    private PayEventService payEventService;
    @Mock
    private TimeProvider timeProvider;
    @InjectMocks
    private OrderExpireService orderExpireService;

    @Test
    void expiresPendingOrdersAndCreatesExpireEvents() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 11, 22, 30);
        XpPayOrder order = new XpPayOrder();
        order.setOrderNo("XP202605110001");
        order.setOrderStatus(OrderStatus.PENDING.name());
        order.setExpireAt(now.minusSeconds(1));
        when(timeProvider.now()).thenReturn(now);
        when(orderMapper.selectList(any())).thenReturn(List.of(order));

        int expired = orderExpireService.expireDueOrders();

        assertThat(expired).isEqualTo(1);
        ArgumentCaptor<XpPayOrder> captor = ArgumentCaptor.forClass(XpPayOrder.class);
        verify(orderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getOrderStatus()).isEqualTo(OrderStatus.EXPIRED.name());
        assertThat(captor.getValue().getUpdatedAt()).isEqualTo(now);
        verify(payEventService).createOrderExpiredEvent(order);
    }
}
