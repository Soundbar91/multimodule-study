package com.soundbar91.shop.domain.event;

import com.soundbar91.shop.domain.vo.ShopCategory;
import java.time.LocalDateTime;

/**
 * 상점 생성 이벤트
 * 도메인 간 느슨한 결합을 위한 이벤트 객체
 */
public class ShopCreatedEvent {

    private final Long shopId;
    private final String name;
    private final ShopCategory category;
    private final Long ownerId;
    private final LocalDateTime occurredAt;

    public ShopCreatedEvent(Long shopId, String name, ShopCategory category, Long ownerId) {
        this.shopId = shopId;
        this.name = name;
        this.category = category;
        this.ownerId = ownerId;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getShopId() {
        return shopId;
    }

    public String getName() {
        return name;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "ShopCreatedEvent{" +
                "shopId=" + shopId +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", ownerId=" + ownerId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
