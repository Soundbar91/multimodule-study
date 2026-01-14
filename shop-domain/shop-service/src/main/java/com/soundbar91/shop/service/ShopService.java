package com.soundbar91.shop.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.event.ShopCreatedEvent;
import com.soundbar91.shop.domain.repository.ShopRepository;
import com.soundbar91.shop.domain.vo.ShopCategory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상점 서비스
 * 도메인 이벤트를 발행하여 다른 도메인과 느슨하게 결합
 */
@Service
@Transactional(readOnly = true)
public class ShopService {

    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ShopService(ShopRepository shopRepository, ApplicationEventPublisher eventPublisher) {
        this.shopRepository = shopRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 상점 생성
     * 생성 후 ShopCreatedEvent 발행
     */
    @Transactional
    public Shop createShop(String name, ShopCategory category, String description, String address, String phoneNumber, Long ownerId) {
        Shop shop = new Shop(name, category, description, address, phoneNumber, ownerId);
        Shop savedShop = shopRepository.save(shop);

        // 도메인 이벤트 발행
        eventPublisher.publishEvent(new ShopCreatedEvent(
                savedShop.getId(),
                savedShop.getName(),
                savedShop.getCategory(),
                savedShop.getOwnerId()
        ));

        return savedShop;
    }

    /**
     * 상점 조회
     */
    public Shop getShopById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상점을 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 모든 상점 조회
     */
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    /**
     * 카테고리별 상점 조회
     */
    public List<Shop> getShopsByCategory(ShopCategory category) {
        return shopRepository.findByCategory(category);
    }

    /**
     * 소유자별 상점 조회
     */
    public List<Shop> getShopsByOwnerId(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId);
    }

    /**
     * 활성 상점만 조회
     */
    public List<Shop> getActiveShops() {
        return shopRepository.findByIsActiveTrue();
    }

    /**
     * 상점 정보 수정
     */
    @Transactional
    public Shop updateShopInfo(Long id, String name, String description, String address, String phoneNumber) {
        Shop shop = getShopById(id);
        shop.updateInfo(name, description, address, phoneNumber);
        return shop;
    }

    /**
     * 상점 카테고리 변경
     */
    @Transactional
    public Shop updateShopCategory(Long id, ShopCategory category) {
        Shop shop = getShopById(id);
        shop.updateCategory(category);
        return shop;
    }

    /**
     * 상점 활성화
     */
    @Transactional
    public Shop activateShop(Long id) {
        Shop shop = getShopById(id);
        shop.activate();
        return shop;
    }

    /**
     * 상점 비활성화
     */
    @Transactional
    public Shop deactivateShop(Long id) {
        Shop shop = getShopById(id);
        shop.deactivate();
        return shop;
    }

    /**
     * 상점 삭제
     */
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = getShopById(id);
        shopRepository.delete(shop);
    }

    /**
     * 상점 존재 여부 확인
     * 다른 도메인에서 상점 검증 시 사용
     */
    public boolean existsById(Long id) {
        return shopRepository.findById(id).isPresent();
    }
}
