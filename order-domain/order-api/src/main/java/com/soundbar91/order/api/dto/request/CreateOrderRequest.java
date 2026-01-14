package com.soundbar91.order.api.dto.request;

import java.math.BigDecimal;

/**
 * 주문 생성 요청 DTO
 */
public record CreateOrderRequest(
        Long userId,
        Long shopId,
        String productName,
        Integer quantity,
        BigDecimal totalAmount,
        String deliveryAddress
) {
}
