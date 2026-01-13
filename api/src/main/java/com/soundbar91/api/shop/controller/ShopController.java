package com.soundbar91.api.shop.controller;

import com.soundbar91.domain.shop.entity.Shop;
import com.soundbar91.domain.shop.service.ShopService;
import com.soundbar91.api.shop.dto.request.CreateShopRequest;
import com.soundbar91.api.shop.dto.request.UpdateShopRequest;
import com.soundbar91.api.shop.dto.response.ShopResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상점 API Controller
 */
@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * 상점 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShopResponse createShop(@RequestBody CreateShopRequest request) {
        Shop shop = shopService.createShop(
                request.getName(),
                request.getCategory(),
                request.getDescription(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getOwnerId()
        );
        return ShopResponse.from(shop);
    }

    /**
     * 상점 조회
     */
    @GetMapping("/{id}")
    public ShopResponse getShop(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        return ShopResponse.from(shop);
    }

    /**
     * 모든 상점 조회
     */
    @GetMapping
    public List<ShopResponse> getAllShops() {
        return shopService.getAllShops().stream()
                .map(ShopResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 소유자별 상점 조회
     */
    @GetMapping("/owner/{ownerId}")
    public List<ShopResponse> getShopsByOwnerId(@PathVariable Long ownerId) {
        return shopService.getShopsByOwnerId(ownerId).stream()
                .map(ShopResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상점 조회
     */
    @GetMapping("/active")
    public List<ShopResponse> getActiveShops() {
        return shopService.getActiveShops().stream()
                .map(ShopResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상점 정보 수정
     */
    @PutMapping("/{id}")
    public ShopResponse updateShop(@PathVariable Long id, @RequestBody UpdateShopRequest request) {
        Shop shop = shopService.updateShopInfo(
                id,
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getPhoneNumber()
        );
        return ShopResponse.from(shop);
    }

    /**
     * 상점 활성화
     */
    @PostMapping("/{id}/activate")
    public ShopResponse activateShop(@PathVariable Long id) {
        Shop shop = shopService.activateShop(id);
        return ShopResponse.from(shop);
    }

    /**
     * 상점 비활성화
     */
    @PostMapping("/{id}/deactivate")
    public ShopResponse deactivateShop(@PathVariable Long id) {
        Shop shop = shopService.deactivateShop(id);
        return ShopResponse.from(shop);
    }

    /**
     * 상점 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
    }
}
