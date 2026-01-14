package com.soundbar91.shop.api.controller;

import com.soundbar91.shop.api.dto.request.CreateShopRequest;
import com.soundbar91.shop.api.dto.request.UpdateShopRequest;
import com.soundbar91.shop.api.dto.response.ShopResponse;
import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;
import com.soundbar91.shop.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상점 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * 상점 생성
     */
    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@RequestBody CreateShopRequest request) {
        Shop shop = shopService.createShop(
                request.name(),
                request.category(),
                request.description(),
                request.address(),
                request.phoneNumber(),
                request.ownerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ShopResponse.from(shop));
    }

    /**
     * 상점 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        return ResponseEntity.ok(ShopResponse.from(shop));
    }

    /**
     * 모든 상점 조회
     */
    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        List<ShopResponse> shops = shopService.getAllShops().stream()
                .map(ShopResponse::from)
                .toList();
        return ResponseEntity.ok(shops);
    }

    /**
     * 카테고리별 상점 조회
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ShopResponse>> getShopsByCategory(@PathVariable ShopCategory category) {
        List<ShopResponse> shops = shopService.getShopsByCategory(category).stream()
                .map(ShopResponse::from)
                .toList();
        return ResponseEntity.ok(shops);
    }

    /**
     * 상점 정보 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> updateShop(@PathVariable Long id, @RequestBody UpdateShopRequest request) {
        Shop shop = shopService.updateShopInfo(id, request.name(), request.description(), request.address(), request.phoneNumber());
        return ResponseEntity.ok(ShopResponse.from(shop));
    }

    /**
     * 상점 활성화
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ShopResponse> activateShop(@PathVariable Long id) {
        Shop shop = shopService.activateShop(id);
        return ResponseEntity.ok(ShopResponse.from(shop));
    }

    /**
     * 상점 비활성화
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ShopResponse> deactivateShop(@PathVariable Long id) {
        Shop shop = shopService.deactivateShop(id);
        return ResponseEntity.ok(ShopResponse.from(shop));
    }

    /**
     * 상점 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
