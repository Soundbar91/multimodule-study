package com.soundbar91.api.shop.dto.request;

/**
 * 상점 수정 요청 DTO
 */
public class UpdateShopRequest {

    private String name;
    private String description;
    private String address;
    private String phoneNumber;

    public UpdateShopRequest() {
    }

    public UpdateShopRequest(String name, String description, String address, String phoneNumber) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
