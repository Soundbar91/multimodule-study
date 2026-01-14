package com.soundbar91.order.domain.entity;

import com.soundbar91.order.domain.vo.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 엔티티
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Order() {
    }

    public Order(Long userId, Long shopId, String productName, Integer quantity, BigDecimal totalAmount, String deliveryAddress) {
        this.userId = userId;
        this.shopId = shopId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 확정할 수 있습니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if (this.status != OrderStatus.CONFIRMED && this.status != OrderStatus.PREPARING) {
            throw new IllegalStateException("확정되거나 준비 중인 주문만 배송할 수 있습니다.");
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("배송 중인 주문만 배송 완료할 수 있습니다.");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    // Getters
    public Long getId() {
        return id;
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

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
