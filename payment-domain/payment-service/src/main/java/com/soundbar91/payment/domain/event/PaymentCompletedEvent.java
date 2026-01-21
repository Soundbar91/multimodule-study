package com.soundbar91.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 완료 이벤트
 * 도메인 간 느슨한 결합을 위한 이벤트 객체
 */
public class PaymentCompletedEvent {

    private final Long paymentId;
    private final Long orderId;
    private final Long userId;
    private final BigDecimal amount;
    private final String transactionId;
    private final LocalDateTime occurredAt;

    public PaymentCompletedEvent(Long paymentId, Long orderId, Long userId, BigDecimal amount, String transactionId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.transactionId = transactionId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "PaymentCompletedEvent{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", transactionId='" + transactionId + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
