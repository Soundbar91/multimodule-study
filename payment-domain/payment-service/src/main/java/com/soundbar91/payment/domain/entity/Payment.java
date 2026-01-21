package com.soundbar91.payment.domain.entity;

import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 엔티티
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 100)
    private String transactionId;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private LocalDateTime refundedAt;

    protected Payment() {
    }

    public Payment(Long orderId, Long userId, BigDecimal amount, PaymentMethod paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void process() {
        if (!this.status.canProcess()) {
            throw new IllegalStateException("처리할 수 없는 결제 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.PROCESSING;
        this.transactionId = generateTransactionId();
    }

    public void complete() {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 결제만 완료할 수 있습니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 결제만 실패 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void refund() {
        if (!this.status.canRefund()) {
            throw new IllegalStateException("환불할 수 없는 결제 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 결제 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.CANCELLED;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters
    public Long getId() {
        return id;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
}
