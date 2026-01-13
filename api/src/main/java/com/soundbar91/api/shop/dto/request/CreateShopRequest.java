package com.soundbar91.api.shop.dto.request;

import com.soundbar91.domain.shop.vo.ShopCategory;

/**
 * 상점 생성 요청 DTO
 */
public class CreateShopRequest {

    private String name;
    private ShopCategory category;
    private String description;
    private String address;
    private String phoneNumber;
    private Long ownerId;

    public CreateShopRequest() {
    }

    public CreateShopRequest(String name, ShopCategory category, String description, String address, String phoneNumber, Long ownerId) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public void setCategory(ShopCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
