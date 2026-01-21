package com.soundbar91.payment.domain.vo;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING("결제 대기"),
    PROCESSING("결제 처리 중"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    REFUNDED("환불 완료"),
    CANCELLED("결제 취소");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canProcess() {
        return this == PENDING;
    }

    public boolean canRefund() {
        return this == COMPLETED;
    }

    public boolean canCancel() {
        return this == PENDING || this == PROCESSING;
    }
}
