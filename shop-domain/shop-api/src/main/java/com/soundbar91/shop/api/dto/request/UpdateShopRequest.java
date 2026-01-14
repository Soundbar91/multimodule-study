package com.soundbar91.shop.api.dto.request;

/**
 * 상점 수정 요청 DTO
 */
public record UpdateShopRequest(
        String name,
        String description,
        String address,
        String phoneNumber
) {
}
