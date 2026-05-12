package com.xiao.xiaopay.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.security.SignedBodyVerifier;
import com.xiao.xiaopay.domain.app.entity.XpApp;
import com.xiao.xiaopay.domain.order.dto.CreateOrderRequest;
import com.xiao.xiaopay.domain.order.dto.CreateOrderResponse;
import com.xiao.xiaopay.domain.order.dto.OrderStatusResponse;
import com.xiao.xiaopay.domain.order.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户侧支付订单接口。
 *
 * <p>创建订单接口必须使用 appSecret 对原始请求体签名。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay/orders")
public class PayOrderController {
    private final PayOrderService payOrderService;
    private final SignedBodyVerifier signedBodyVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ApiResponse<CreateOrderResponse> create(@RequestHeader("X-XiaoPay-App") String appId,
                                                   @RequestHeader("X-XiaoPay-Timestamp") String timestamp,
                                                   @RequestHeader("X-XiaoPay-Nonce") String nonce,
                                                   @RequestHeader("X-XiaoPay-Signature") String signature,
                                                   @RequestBody String body) throws Exception {
        signedBodyVerifier.verifyApp(appId, timestamp, nonce, signature, body);
        CreateOrderRequest request = objectMapper.readValue(body, CreateOrderRequest.class);
        return ApiResponse.ok(payOrderService.create(appId, request));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderStatusResponse> get(@RequestHeader("X-XiaoPay-App") String appId,
                                                @PathVariable String orderNo) {
        return ApiResponse.ok(payOrderService.getOrder(appId, orderNo));
    }

    @PostMapping("/{orderNo}/close")
    public ApiResponse<Void> close(@RequestHeader("X-XiaoPay-App") String appId,
                                   @PathVariable String orderNo) {
        payOrderService.close(appId, orderNo);
        return ApiResponse.ok();
    }
}
