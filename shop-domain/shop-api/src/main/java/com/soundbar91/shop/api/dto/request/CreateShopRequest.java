package com.soundbar91.shop.api.dto.request;

import com.soundbar91.shop.domain.vo.ShopCategory;

/**
 * 상점 생성 요청 DTO
 */
public record CreateShopRequest(
        String name,
        ShopCategory category,
        String description,
        String address,
        String phoneNumber,
        Long ownerId
) {
}
