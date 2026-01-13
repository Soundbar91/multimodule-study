package com.soundbar91.api.user.dto.request;

import com.soundbar91.domain.user.vo.UserRole;

/**
 * 사용자 생성 요청 DTO
 */
public class CreateUserRequest {

    private String name;
    private String email;
    private String phoneNumber;
    private UserRole role;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String email, String phoneNumber, UserRole role) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
