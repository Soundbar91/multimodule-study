package com.soundbar91.order.api.dto.response;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 */
public record OrderResponse(
        Long id,
        Long userId,
        Long shopId,
        String productName,
        Integer quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String deliveryAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getShopId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
