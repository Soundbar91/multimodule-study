package com.soundbar91.shop.infrastructure.repository;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.repository.ShopRepository;
import com.soundbar91.shop.domain.vo.ShopCategory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ShopRepository 구현체
 * JPA를 사용한 영속성 계층 구현
 */
@Repository
public class ShopRepositoryImpl implements ShopRepository {

    private final ShopJpaRepository shopJpaRepository;

    public ShopRepositoryImpl(ShopJpaRepository shopJpaRepository) {
        this.shopJpaRepository = shopJpaRepository;
    }

    @Override
    public Shop save(Shop shop) {
        return shopJpaRepository.save(shop);
    }

    @Override
    public Optional<Shop> findById(Long id) {
        return shopJpaRepository.findById(id);
    }

    @Override
    public List<Shop> findAll() {
        return shopJpaRepository.findAll();
    }

    @Override
    public List<Shop> findByCategory(ShopCategory category) {
        return shopJpaRepository.findByCategory(category);
    }

    @Override
    public List<Shop> findByOwnerId(Long ownerId) {
        return shopJpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Shop> findByIsActiveTrue() {
        return shopJpaRepository.findByIsActiveTrue();
    }

    @Override
    public void delete(Shop shop) {
        shopJpaRepository.delete(shop);
    }

    @Override
    public void deleteById(Long id) {
        shopJpaRepository.deleteById(id);
    }
}
