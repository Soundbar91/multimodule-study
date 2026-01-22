package com.soundbar91.order.infrastructure.repository;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderJpaRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderJpaRepository 테스트")
class OrderJpaRepositoryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.soundbar91.order.domain.entity")
    @EnableJpaRepositories(basePackages = "com.soundbar91.order.infrastructure.repository")
    static class TestConfig {}

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("새로운 주문을 저장하면 ID가 생성된다")
        void save_NewOrder_GeneratesId() {
            // given
            Order order = new Order(1L, 1L, "테스트 상품", 2, BigDecimal.valueOf(50000), "서울시 강남구");

            // when
            Order savedOrder = orderJpaRepository.save(order);

            // then
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(savedOrder.getProductName()).isEqualTo("테스트 상품");
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 주문을 반환한다")
        void findById_WithExistingId_ReturnsOrder() {
            // given
            Order order = new Order(1L, 1L, "조회 테스트 상품", 1, BigDecimal.valueOf(30000), "주소");
            Order persistedOrder = orderJpaRepository.save(order);

            // when
            Optional<Order> found = orderJpaRepository.findById(persistedOrder.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getProductName()).isEqualTo("조회 테스트 상품");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void findById_WithNonExistingId_ReturnsEmpty() {
            // when
            Optional<Order> found = orderJpaRepository.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId 메서드")
    class FindByUserId {

        @Test
        @DisplayName("특정 사용자의 주문만 조회한다")
        void findByUserId_ReturnsUserOrders() {
            // given
            Long userId = 1L;
            Order order1 = new Order(userId, 1L, "상품1", 1, BigDecimal.valueOf(10000), "주소1");
            Order order2 = new Order(userId, 2L, "상품2", 2, BigDecimal.valueOf(20000), "주소2");
            Order otherUserOrder = new Order(2L, 1L, "다른사용자상품", 1, BigDecimal.valueOf(15000), "주소3");

            orderJpaRepository.save(order1);
            orderJpaRepository.save(order2);
            orderJpaRepository.save(otherUserOrder);

            // when
            List<Order> userOrders = orderJpaRepository.findByUserId(userId);

            // then
            assertThat(userOrders).hasSize(2);
            assertThat(userOrders).allMatch(o -> o.getUserId().equals(userId));
        }
    }

    @Nested
    @DisplayName("findByShopId 메서드")
    class FindByShopId {

        @Test
        @DisplayName("특정 상점의 주문만 조회한다")
        void findByShopId_ReturnsShopOrders() {
            // given
            Long shopId = 1L;
            Order order1 = new Order(1L, shopId, "상품1", 1, BigDecimal.valueOf(10000), "주소1");
            Order order2 = new Order(2L, shopId, "상품2", 2, BigDecimal.valueOf(20000), "주소2");
            Order otherShopOrder = new Order(1L, 2L, "다른상점상품", 1, BigDecimal.valueOf(15000), "주소3");

            orderJpaRepository.save(order1);
            orderJpaRepository.save(order2);
            orderJpaRepository.save(otherShopOrder);

            // when
            List<Order> shopOrders = orderJpaRepository.findByShopId(shopId);

            // then
            assertThat(shopOrders).hasSize(2);
            assertThat(shopOrders).allMatch(o -> o.getShopId().equals(shopId));
        }
    }

    @Nested
    @DisplayName("findByStatus 메서드")
    class FindByStatus {

        @Test
        @DisplayName("특정 상태의 주문만 조회한다")
        void findByStatus_ReturnsOrdersWithStatus() {
            // given
            Order pendingOrder1 = new Order(1L, 1L, "대기상품1", 1, BigDecimal.valueOf(10000), "주소1");
            Order pendingOrder2 = new Order(2L, 2L, "대기상품2", 1, BigDecimal.valueOf(20000), "주소2");
            Order confirmedOrder = new Order(3L, 3L, "확정상품", 1, BigDecimal.valueOf(30000), "주소3");
            confirmedOrder.confirm();

            orderJpaRepository.save(pendingOrder1);
            orderJpaRepository.save(pendingOrder2);
            orderJpaRepository.save(confirmedOrder);

            // when
            List<Order> pendingOrders = orderJpaRepository.findByStatus(OrderStatus.PENDING);
            List<Order> confirmedOrders = orderJpaRepository.findByStatus(OrderStatus.CONFIRMED);

            // then
            assertThat(pendingOrders).hasSize(2);
            assertThat(confirmedOrders).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("주문을 삭제하면 조회되지 않는다")
        void delete_RemovesOrder() {
            // given
            Order order = new Order(1L, 1L, "삭제될 주문", 1, BigDecimal.valueOf(10000), "주소");
            Order persistedOrder = orderJpaRepository.save(order);
            Long orderId = persistedOrder.getId();

            // when
            orderJpaRepository.delete(persistedOrder);
            orderJpaRepository.flush();

            // then
            Optional<Order> found = orderJpaRepository.findById(orderId);
            assertThat(found).isEmpty();
        }
    }
}
