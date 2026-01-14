package com.soundbar91.shop.api.dto.response;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;

import java.time.LocalDateTime;

/**
 * 상점 응답 DTO
 */
public record ShopResponse(
        Long id,
        String name,
        ShopCategory category,
        String description,
        String address,
        String phoneNumber,
        Long ownerId,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ShopResponse from(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getCategory(),
                shop.getDescription(),
                shop.getAddress(),
                shop.getPhoneNumber(),
                shop.getOwnerId(),
                shop.getIsActive(),
                shop.getCreatedAt(),
                shop.getUpdatedAt()
        );
    }
}
