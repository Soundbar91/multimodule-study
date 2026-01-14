package com.soundbar91.order.domain.vo;

/**
 * 주문 상태
 */
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    PREPARING("준비 중"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }
}
