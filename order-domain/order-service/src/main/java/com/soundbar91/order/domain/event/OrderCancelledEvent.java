package com.soundbar91.order.domain.event;

import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 * 도메인 간 느슨한 결합을 위한 이벤트 객체
 */
public class OrderCancelledEvent {

    private final Long orderId;
    private final Long userId;
    private final Long shopId;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(Long orderId, Long userId, Long shopId) {
        this.orderId = orderId;
        this.userId = userId;
        this.shopId = shopId;
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

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "OrderCancelledEvent{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", shopId=" + shopId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
