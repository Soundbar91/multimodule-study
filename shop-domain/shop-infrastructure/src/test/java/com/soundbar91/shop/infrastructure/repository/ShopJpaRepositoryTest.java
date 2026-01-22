package com.soundbar91.shop.infrastructure.repository;

import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShopJpaRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("ShopJpaRepository 테스트")
class ShopJpaRepositoryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.soundbar91.shop.domain.entity")
    @EnableJpaRepositories(basePackages = "com.soundbar91.shop.infrastructure.repository")
    static class TestConfig {}

    @Autowired
    private ShopJpaRepository shopJpaRepository;

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("새로운 상점을 저장하면 ID가 생성된다")
        void save_NewShop_GeneratesId() {
            // given
            Shop shop = new Shop("테스트 상점", ShopCategory.RESTAURANT, "맛있는 음식점",
                    "서울시 강남구", "02-1234-5678", 1L);

            // when
            Shop savedShop = shopJpaRepository.save(shop);

            // then
            assertThat(savedShop.getId()).isNotNull();
            assertThat(savedShop.getName()).isEqualTo("테스트 상점");
            assertThat(savedShop.getIsActive()).isTrue();
            assertThat(savedShop.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 상점을 반환한다")
        void findById_WithExistingId_ReturnsShop() {
            // given
            Shop shop = new Shop("테스트 상점", ShopCategory.CAFE, "분위기 좋은 카페",
                    "서울시 마포구", "02-2222-3333", 1L);
            Shop persistedShop = shopJpaRepository.save(shop);

            // when
            Optional<Shop> found = shopJpaRepository.findById(persistedShop.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("테스트 상점");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void findById_WithNonExistingId_ReturnsEmpty() {
            // when
            Optional<Shop> found = shopJpaRepository.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCategory 메서드")
    class FindByCategory {

        @Test
        @DisplayName("특정 카테고리의 상점만 조회한다")
        void findByCategory_ReturnsMatchingShops() {
            // given
            Shop cafe1 = new Shop("카페1", ShopCategory.CAFE, "설명", "주소1", "010-1111-1111", 1L);
            Shop cafe2 = new Shop("카페2", ShopCategory.CAFE, "설명", "주소2", "010-2222-2222", 2L);
            Shop restaurant = new Shop("음식점", ShopCategory.RESTAURANT, "설명", "주소3", "010-3333-3333", 3L);

            shopJpaRepository.save(cafe1);
            shopJpaRepository.save(cafe2);
            shopJpaRepository.save(restaurant);

            // when
            List<Shop> cafes = shopJpaRepository.findByCategory(ShopCategory.CAFE);

            // then
            assertThat(cafes).hasSize(2);
            assertThat(cafes).allMatch(shop -> shop.getCategory() == ShopCategory.CAFE);
        }
    }

    @Nested
    @DisplayName("findByOwnerId 메서드")
    class FindByOwnerId {

        @Test
        @DisplayName("특정 소유자의 상점만 조회한다")
        void findByOwnerId_ReturnsOwnerShops() {
            // given
            Long ownerId = 1L;
            Shop shop1 = new Shop("상점1", ShopCategory.RETAIL, "설명", "주소1", "010-1111-1111", ownerId);
            Shop shop2 = new Shop("상점2", ShopCategory.FASHION, "설명", "주소2", "010-2222-2222", ownerId);
            Shop otherShop = new Shop("다른상점", ShopCategory.GROCERY, "설명", "주소3", "010-3333-3333", 2L);

            shopJpaRepository.save(shop1);
            shopJpaRepository.save(shop2);
            shopJpaRepository.save(otherShop);

            // when
            List<Shop> ownerShops = shopJpaRepository.findByOwnerId(ownerId);

            // then
            assertThat(ownerShops).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByIsActiveTrue 메서드")
    class FindByIsActiveTrue {

        @Test
        @DisplayName("활성 상태인 상점만 조회한다")
        void findByIsActiveTrue_ReturnsActiveShops() {
            // given
            Shop activeShop1 = new Shop("활성상점1", ShopCategory.CAFE, "설명", "주소1", "010-1111-1111", 1L);
            Shop activeShop2 = new Shop("활성상점2", ShopCategory.RETAIL, "설명", "주소2", "010-2222-2222", 2L);
            Shop inactiveShop = new Shop("비활성상점", ShopCategory.RESTAURANT, "설명", "주소3", "010-3333-3333", 3L);
            inactiveShop.deactivate();

            shopJpaRepository.save(activeShop1);
            shopJpaRepository.save(activeShop2);
            shopJpaRepository.save(inactiveShop);

            // when
            List<Shop> activeShops = shopJpaRepository.findByIsActiveTrue();

            // then
            assertThat(activeShops).hasSize(2);
            assertThat(activeShops).allMatch(Shop::getIsActive);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("상점을 삭제하면 조회되지 않는다")
        void delete_RemovesShop() {
            // given
            Shop shop = new Shop("삭제될 상점", ShopCategory.OTHER, "설명", "주소", "010-1234-5678", 1L);
            Shop persistedShop = shopJpaRepository.save(shop);
            Long shopId = persistedShop.getId();

            // when
            shopJpaRepository.delete(persistedShop);
            shopJpaRepository.flush();

            // then
            Optional<Shop> found = shopJpaRepository.findById(shopId);
            assertThat(found).isEmpty();
        }
    }
}
