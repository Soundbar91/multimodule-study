package com.soundbar91.shop.domain.repository;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;

import java.util.List;
import java.util.Optional;

/**
 * 상점 레포지토리 인터페이스
 * 도메인 계층에서 정의하고, infrastructure 계층에서 구현
 */
public interface ShopRepository {

    Shop save(Shop shop);

    Optional<Shop> findById(Long id);

    List<Shop> findAll();

    List<Shop> findByCategory(ShopCategory category);

    List<Shop> findByOwnerId(Long ownerId);

    List<Shop> findByIsActiveTrue();

    void delete(Shop shop);

    void deleteById(Long id);
}
