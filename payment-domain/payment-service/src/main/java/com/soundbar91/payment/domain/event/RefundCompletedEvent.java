package com.soundbar91.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 완료 이벤트
 * 도메인 간 느슨한 결합을 위한 이벤트 객체
 */
public class RefundCompletedEvent {

    private final Long paymentId;
    private final Long orderId;
    private final Long userId;
    private final BigDecimal refundAmount;
    private final LocalDateTime occurredAt;

    public RefundCompletedEvent(Long paymentId, Long orderId, Long userId, BigDecimal refundAmount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "RefundCompletedEvent{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", refundAmount=" + refundAmount +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
