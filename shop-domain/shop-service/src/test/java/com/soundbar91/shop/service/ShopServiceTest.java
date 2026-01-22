package com.soundbar91.shop.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.event.ShopCreatedEvent;
import com.soundbar91.shop.domain.repository.ShopRepository;
import com.soundbar91.shop.domain.vo.ShopCategory;
import com.soundbar91.test.fixture.ShopFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShopService 단위 테스트")
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShopService shopService;

    @Nested
    @DisplayName("createShop 메서드")
    class CreateShop {

        @Test
        @DisplayName("유효한 정보로 상점을 생성하면 저장된 상점을 반환한다")
        void createShop_WithValidInfo_ReturnsShop() {
            // given
            String name = "테스트 상점";
            ShopCategory category = ShopCategory.RESTAURANT;
            String description = "맛있는 음식점";
            String address = "서울시 강남구";
            String phoneNumber = "02-1234-5678";
            Long ownerId = 1L;

            Shop expectedShop = ShopFixture.create()
                    .withId(1L)
                    .withName(name)
                    .withCategory(category)
                    .withDescription(description)
                    .withAddress(address)
                    .withPhoneNumber(phoneNumber)
                    .withOwnerId(ownerId)
                    .build();

            given(shopRepository.save(any(Shop.class))).willReturn(expectedShop);

            // when
            Shop result = shopService.createShop(name, category, description, address, phoneNumber, ownerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getCategory()).isEqualTo(category);
            assertThat(result.getIsActive()).isTrue();

            then(shopRepository).should().save(any(Shop.class));
            then(eventPublisher).should().publishEvent(any(ShopCreatedEvent.class));
        }

        @Test
        @DisplayName("상점 생성 시 ShopCreatedEvent가 발행된다")
        void createShop_PublishesShopCreatedEvent() {
            // given
            Shop savedShop = ShopFixture.create()
                    .withId(1L)
                    .withName("이벤트 테스트 상점")
                    .withCategory(ShopCategory.CAFE)
                    .withOwnerId(2L)
                    .build();

            given(shopRepository.save(any(Shop.class))).willReturn(savedShop);

            ArgumentCaptor<ShopCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ShopCreatedEvent.class);

            // when
            shopService.createShop("이벤트 테스트 상점", ShopCategory.CAFE, "설명", "주소", "전화", 2L);

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            ShopCreatedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getShopId()).isEqualTo(1L);
            assertThat(capturedEvent.getName()).isEqualTo("이벤트 테스트 상점");
            assertThat(capturedEvent.getCategory()).isEqualTo(ShopCategory.CAFE);
            assertThat(capturedEvent.getOwnerId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("getShopById 메서드")
    class GetShopById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 상점을 반환한다")
        void getShopById_WithExistingId_ReturnsShop() {
            // given
            Long shopId = 1L;
            Shop shop = ShopFixture.create().withId(shopId).build();
            given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));

            // when
            Shop result = shopService.getShopById(shopId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(shopId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void getShopById_WithNonExistingId_ThrowsException() {
            // given
            Long shopId = 999L;
            given(shopRepository.findById(shopId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.getShopById(shopId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("상점을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getShopsByCategory 메서드")
    class GetShopsByCategory {

        @Test
        @DisplayName("특정 카테고리의 상점 목록을 반환한다")
        void getShopsByCategory_ReturnsShopsWithCategory() {
            // given
            ShopCategory category = ShopCategory.CAFE;
            List<Shop> cafes = List.of(
                    ShopFixture.create().withId(1L).withCategory(category).build(),
                    ShopFixture.create().withId(2L).withCategory(category).build()
            );
            given(shopRepository.findByCategory(category)).willReturn(cafes);

            // when
            List<Shop> result = shopService.getShopsByCategory(category);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(shop -> shop.getCategory() == category);
        }
    }

    @Nested
    @DisplayName("getShopsByOwnerId 메서드")
    class GetShopsByOwnerId {

        @Test
        @DisplayName("특정 소유자의 상점 목록을 반환한다")
        void getShopsByOwnerId_ReturnsOwnerShops() {
            // given
            Long ownerId = 1L;
            List<Shop> ownerShops = List.of(
                    ShopFixture.create().withId(1L).withOwnerId(ownerId).build(),
                    ShopFixture.create().withId(2L).withOwnerId(ownerId).build()
            );
            given(shopRepository.findByOwnerId(ownerId)).willReturn(ownerShops);

            // when
            List<Shop> result = shopService.getShopsByOwnerId(ownerId);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getActiveShops 메서드")
    class GetActiveShops {

        @Test
        @DisplayName("활성 상태인 상점만 반환한다")
        void getActiveShops_ReturnsOnlyActiveShops() {
            // given
            List<Shop> activeShops = List.of(
                    ShopFixture.create().withId(1L).build(),
                    ShopFixture.create().withId(2L).build()
            );
            given(shopRepository.findByIsActiveTrue()).willReturn(activeShops);

            // when
            List<Shop> result = shopService.getActiveShops();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateShopInfo 메서드")
    class UpdateShopInfo {

        @Test
        @DisplayName("유효한 정보로 상점을 수정하면 수정된 상점을 반환한다")
        void updateShopInfo_WithValidInfo_ReturnsUpdatedShop() {
            // given
            Long shopId = 1L;
            String newName = "수정된 상점";
            String newDescription = "수정된 설명";
            String newAddress = "새로운 주소";
            String newPhone = "02-9999-8888";

            Shop existingShop = ShopFixture.create()
                    .withId(shopId)
                    .withName("기존 상점")
                    .build();

            given(shopRepository.findById(shopId)).willReturn(Optional.of(existingShop));

            // when
            Shop result = shopService.updateShopInfo(shopId, newName, newDescription, newAddress, newPhone);

            // then
            assertThat(result.getName()).isEqualTo(newName);
            assertThat(result.getDescription()).isEqualTo(newDescription);
            assertThat(result.getAddress()).isEqualTo(newAddress);
            assertThat(result.getPhoneNumber()).isEqualTo(newPhone);
        }
    }

    @Nested
    @DisplayName("updateShopCategory 메서드")
    class UpdateShopCategory {

        @Test
        @DisplayName("상점 카테고리를 변경하면 수정된 상점을 반환한다")
        void updateShopCategory_ReturnsUpdatedShop() {
            // given
            Long shopId = 1L;
            ShopCategory newCategory = ShopCategory.FASHION;

            Shop existingShop = ShopFixture.create()
                    .withId(shopId)
                    .withCategory(ShopCategory.RETAIL)
                    .build();

            given(shopRepository.findById(shopId)).willReturn(Optional.of(existingShop));

            // when
            Shop result = shopService.updateShopCategory(shopId, newCategory);

            // then
            assertThat(result.getCategory()).isEqualTo(newCategory);
        }
    }

    @Nested
    @DisplayName("activateShop 메서드")
    class ActivateShop {

        @Test
        @DisplayName("비활성 상점을 활성화하면 isActive가 true가 된다")
        void activateShop_MakesShopActive() {
            // given
            Long shopId = 1L;
            Shop inactiveShop = ShopFixture.create()
                    .withId(shopId)
                    .withIsActive(false)
                    .build();

            given(shopRepository.findById(shopId)).willReturn(Optional.of(inactiveShop));

            // when
            Shop result = shopService.activateShop(shopId);

            // then
            assertThat(result.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("deactivateShop 메서드")
    class DeactivateShop {

        @Test
        @DisplayName("활성 상점을 비활성화하면 isActive가 false가 된다")
        void deactivateShop_MakesShopInactive() {
            // given
            Long shopId = 1L;
            Shop activeShop = ShopFixture.create()
                    .withId(shopId)
                    .build();

            given(shopRepository.findById(shopId)).willReturn(Optional.of(activeShop));

            // when
            Shop result = shopService.deactivateShop(shopId);

            // then
            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteShop 메서드")
    class DeleteShop {

        @Test
        @DisplayName("존재하는 상점을 삭제하면 정상적으로 삭제된다")
        void deleteShop_WithExistingShop_DeletesSuccessfully() {
            // given
            Long shopId = 1L;
            Shop existingShop = ShopFixture.create().withId(shopId).build();
            given(shopRepository.findById(shopId)).willReturn(Optional.of(existingShop));

            // when
            shopService.deleteShop(shopId);

            // then
            then(shopRepository).should().delete(existingShop);
        }
    }

    @Nested
    @DisplayName("existsById 메서드")
    class ExistsById {

        @Test
        @DisplayName("존재하는 상점 ID이면 true를 반환한다")
        void existsById_WithExistingShop_ReturnsTrue() {
            // given
            Long shopId = 1L;
            given(shopRepository.findById(shopId)).willReturn(Optional.of(ShopFixture.createDefault()));

            // when
            boolean result = shopService.existsById(shopId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상점 ID이면 false를 반환한다")
        void existsById_WithNonExistingShop_ReturnsFalse() {
            // given
            Long shopId = 999L;
            given(shopRepository.findById(shopId)).willReturn(Optional.empty());

            // when
            boolean result = shopService.existsById(shopId);

            // then
            assertThat(result).isFalse();
        }
    }
}
