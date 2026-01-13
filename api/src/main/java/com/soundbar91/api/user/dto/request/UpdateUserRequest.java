package com.soundbar91.api.user.dto.request;

/**
 * 사용자 수정 요청 DTO
 */
public class UpdateUserRequest {

    private String name;
    private String phoneNumber;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
