package com.soundbar91.api.order.dto.request;

import java.math.BigDecimal;

/**
 * 주문 생성 요청 DTO
 */
public class CreateOrderRequest {

    private Long userId;
    private Long shopId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String deliveryAddress;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long userId, Long shopId, String productName, Integer quantity, BigDecimal totalAmount, String deliveryAddress) {
        this.userId = userId;
        this.shopId = shopId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
