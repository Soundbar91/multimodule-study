package com.soundbar91.shop.infrastructure.repository;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA Repository
 */
public interface ShopJpaRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByCategory(ShopCategory category);

    List<Shop> findByOwnerId(Long ownerId);

    List<Shop> findByIsActiveTrue();
}
