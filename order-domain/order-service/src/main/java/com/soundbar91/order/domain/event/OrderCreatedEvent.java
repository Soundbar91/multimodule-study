package com.soundbar91.order.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 생성 이벤트
 * 도메인 간 느슨한 결합을 위한 이벤트 객체
 */
public class OrderCreatedEvent {

    private final Long orderId;
    private final Long userId;
    private final Long shopId;
    private final String productName;
    private final BigDecimal totalAmount;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(Long orderId, Long userId, Long shopId, String productName, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.shopId = shopId;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", shopId=" + shopId +
                ", productName='" + productName + '\'' +
                ", totalAmount=" + totalAmount +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
