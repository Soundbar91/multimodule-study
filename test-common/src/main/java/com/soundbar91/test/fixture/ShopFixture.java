package com.soundbar91.test.fixture;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;

import java.lang.reflect.Field;

/**
 * Shop 엔티티 테스트 픽스처
 */
public class ShopFixture {

    private static final String DEFAULT_NAME = "테스트 상점";
    private static final ShopCategory DEFAULT_CATEGORY = ShopCategory.RESTAURANT;
    private static final String DEFAULT_DESCRIPTION = "테스트 상점 설명";
    private static final String DEFAULT_ADDRESS = "서울시 강남구 테스트로 123";
    private static final String DEFAULT_PHONE = "02-1234-5678";
    private static final Long DEFAULT_OWNER_ID = 1L;

    private String name = DEFAULT_NAME;
    private ShopCategory category = DEFAULT_CATEGORY;
    private String description = DEFAULT_DESCRIPTION;
    private String address = DEFAULT_ADDRESS;
    private String phoneNumber = DEFAULT_PHONE;
    private Long ownerId = DEFAULT_OWNER_ID;
    private Long id = null;
    private Boolean isActive = null;

    private ShopFixture() {}

    public static ShopFixture create() {
        return new ShopFixture();
    }

    public static Shop createDefault() {
        return new Shop(DEFAULT_NAME, DEFAULT_CATEGORY, DEFAULT_DESCRIPTION,
                       DEFAULT_ADDRESS, DEFAULT_PHONE, DEFAULT_OWNER_ID);
    }

    public static Shop createCafe() {
        return new Shop("테스트 카페", ShopCategory.CAFE, "분위기 좋은 카페",
                       "서울시 마포구 카페로 456", "02-2345-6789", 2L);
    }

    public static Shop createRetailShop() {
        return new Shop("테스트 소매점", ShopCategory.RETAIL, "다양한 상품을 판매하는 소매점",
                       "서울시 서초구 소매로 789", "02-3456-7890", 3L);
    }

    public ShopFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public ShopFixture withName(String name) {
        this.name = name;
        return this;
    }

    public ShopFixture withCategory(ShopCategory category) {
        this.category = category;
        return this;
    }

    public ShopFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public ShopFixture withAddress(String address) {
        this.address = address;
        return this;
    }

    public ShopFixture withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ShopFixture withOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public ShopFixture withIsActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public Shop build() {
        Shop shop = new Shop(name, category, description, address, phoneNumber, ownerId);
        if (id != null) {
            setField(shop, "id", id);
        }
        if (isActive != null && !isActive) {
            shop.deactivate();
        }
        return shop;
    }

    /**
     * 리플렉션을 사용하여 필드 설정 (테스트 전용)
     */
    private static void setField(Shop shop, String fieldName, Object value) {
        try {
            Field field = Shop.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(shop, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field via reflection: " + fieldName, e);
        }
    }
}
