package com.soundbar91.shop.domain.vo;

/**
 * 상점 카테고리
 */
public enum ShopCategory {
    RESTAURANT("음식점"),
    CAFE("카페"),
    RETAIL("소매점"),
    FASHION("패션"),
    ELECTRONICS("전자제품"),
    GROCERY("식료품"),
    OTHER("기타");

    private final String description;

    ShopCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
