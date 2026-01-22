package com.soundbar91.order.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.event.OrderCancelledEvent;
import com.soundbar91.order.domain.event.OrderCreatedEvent;
import com.soundbar91.order.domain.repository.OrderRepository;
import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.shop.service.ShopService;
import com.soundbar91.test.fixture.OrderFixture;
import com.soundbar91.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ShopService shopService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("createOrder 메서드")
    class CreateOrder {

        @Test
        @DisplayName("유효한 정보로 주문을 생성하면 저장된 주문을 반환한다")
        void createOrder_WithValidInfo_ReturnsOrder() {
            // given
            Long userId = 1L;
            Long shopId = 1L;
            String productName = "테스트 상품";
            Integer quantity = 2;
            BigDecimal totalAmount = BigDecimal.valueOf(50000);
            String deliveryAddress = "서울시 강남구";

            Order expectedOrder = OrderFixture.create()
                    .withId(1L)
                    .withUserId(userId)
                    .withShopId(shopId)
                    .withProductName(productName)
                    .build();

            given(userService.existsById(userId)).willReturn(true);
            given(shopService.existsById(shopId)).willReturn(true);
            given(orderRepository.save(any(Order.class))).willReturn(expectedOrder);

            // when
            Order result = orderService.createOrder(userId, shopId, productName, quantity, totalAmount, deliveryAddress);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getShopId()).isEqualTo(shopId);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

            then(eventPublisher).should().publishEvent(any(OrderCreatedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 주문을 생성하면 예외가 발생한다")
        void createOrder_WithNonExistingUser_ThrowsException() {
            // given
            Long userId = 999L;
            given(userService.existsById(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(userId, 1L, "상품", 1, BigDecimal.valueOf(10000), "주소"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("주문자를 찾을 수 없습니다");

            then(orderRepository).should(never()).save(any(Order.class));
        }

        @Test
        @DisplayName("존재하지 않는 상점으로 주문을 생성하면 예외가 발생한다")
        void createOrder_WithNonExistingShop_ThrowsException() {
            // given
            Long userId = 1L;
            Long shopId = 999L;
            given(userService.existsById(userId)).willReturn(true);
            given(shopService.existsById(shopId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(userId, shopId, "상품", 1, BigDecimal.valueOf(10000), "주소"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("상점을 찾을 수 없습니다");

            then(orderRepository).should(never()).save(any(Order.class));
        }

        @Test
        @DisplayName("주문 생성 시 OrderCreatedEvent가 발행된다")
        void createOrder_PublishesOrderCreatedEvent() {
            // given
            Order savedOrder = OrderFixture.create()
                    .withId(1L)
                    .withUserId(1L)
                    .withShopId(1L)
                    .withProductName("이벤트 테스트 상품")
                    .withTotalAmount(BigDecimal.valueOf(30000))
                    .build();

            given(userService.existsById(1L)).willReturn(true);
            given(shopService.existsById(1L)).willReturn(true);
            given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

            ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);

            // when
            orderService.createOrder(1L, 1L, "이벤트 테스트 상품", 1, BigDecimal.valueOf(30000), "주소");

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            OrderCreatedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getOrderId()).isEqualTo(1L);
            assertThat(capturedEvent.getUserId()).isEqualTo(1L);
            assertThat(capturedEvent.getShopId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getOrderById 메서드")
    class GetOrderById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 주문을 반환한다")
        void getOrderById_WithExistingId_ReturnsOrder() {
            // given
            Long orderId = 1L;
            Order order = OrderFixture.create().withId(orderId).build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when
            Order result = orderService.getOrderById(orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void getOrderById_WithNonExistingId_ThrowsException() {
            // given
            Long orderId = 999L;
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderById(orderId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("주문을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getOrdersByUserId 메서드")
    class GetOrdersByUserId {

        @Test
        @DisplayName("사용자별 주문 목록을 반환한다")
        void getOrdersByUserId_ReturnsUserOrders() {
            // given
            Long userId = 1L;
            List<Order> orders = List.of(
                    OrderFixture.create().withId(1L).withUserId(userId).build(),
                    OrderFixture.create().withId(2L).withUserId(userId).build()
            );
            given(orderRepository.findByUserId(userId)).willReturn(orders);

            // when
            List<Order> result = orderService.getOrdersByUserId(userId);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getOrdersByStatus 메서드")
    class GetOrdersByStatus {

        @Test
        @DisplayName("상태별 주문 목록을 반환한다")
        void getOrdersByStatus_ReturnsOrdersWithStatus() {
            // given
            OrderStatus status = OrderStatus.PENDING;
            List<Order> pendingOrders = List.of(
                    OrderFixture.create().withId(1L).withStatus(status).build(),
                    OrderFixture.create().withId(2L).withStatus(status).build()
            );
            given(orderRepository.findByStatus(status)).willReturn(pendingOrders);

            // when
            List<Order> result = orderService.getOrdersByStatus(status);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("confirmOrder 메서드")
    class ConfirmOrder {

        @Test
        @DisplayName("대기 중인 주문을 확정하면 상태가 CONFIRMED로 변경된다")
        void confirmOrder_WithPendingOrder_ChangesStatusToConfirmed() {
            // given
            Long orderId = 1L;
            Order pendingOrder = OrderFixture.create().withId(orderId).build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(pendingOrder));

            // when
            Order result = orderService.confirmOrder(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("shipOrder 메서드")
    class ShipOrder {

        @Test
        @DisplayName("확정된 주문을 배송하면 상태가 SHIPPED로 변경된다")
        void shipOrder_WithConfirmedOrder_ChangesStatusToShipped() {
            // given
            Long orderId = 1L;
            Order confirmedOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.CONFIRMED)
                    .build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(confirmedOrder));

            // when
            Order result = orderService.shipOrder(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }
    }

    @Nested
    @DisplayName("deliverOrder 메서드")
    class DeliverOrder {

        @Test
        @DisplayName("배송 중인 주문을 배송 완료하면 상태가 DELIVERED로 변경된다")
        void deliverOrder_WithShippedOrder_ChangesStatusToDelivered() {
            // given
            Long orderId = 1L;
            Order shippedOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.SHIPPED)
                    .build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(shippedOrder));

            // when
            Order result = orderService.deliverOrder(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }

    @Nested
    @DisplayName("cancelOrder 메서드")
    class CancelOrder {

        @Test
        @DisplayName("대기 중인 주문을 취소하면 상태가 CANCELLED로 변경된다")
        void cancelOrder_WithPendingOrder_ChangesStatusToCancelled() {
            // given
            Long orderId = 1L;
            Order pendingOrder = OrderFixture.create()
                    .withId(orderId)
                    .withUserId(1L)
                    .withShopId(1L)
                    .build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(pendingOrder));

            // when
            Order result = orderService.cancelOrder(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            then(eventPublisher).should().publishEvent(any(OrderCancelledEvent.class));
        }

        @Test
        @DisplayName("주문 취소 시 OrderCancelledEvent가 발행된다")
        void cancelOrder_PublishesOrderCancelledEvent() {
            // given
            Long orderId = 1L;
            Order pendingOrder = OrderFixture.create()
                    .withId(orderId)
                    .withUserId(2L)
                    .withShopId(3L)
                    .build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(pendingOrder));

            ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);

            // when
            orderService.cancelOrder(orderId);

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            OrderCancelledEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getOrderId()).isEqualTo(1L);
            assertThat(capturedEvent.getUserId()).isEqualTo(2L);
            assertThat(capturedEvent.getShopId()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("deleteOrder 메서드")
    class DeleteOrder {

        @Test
        @DisplayName("존재하는 주문을 삭제하면 정상적으로 삭제된다")
        void deleteOrder_WithExistingOrder_DeletesSuccessfully() {
            // given
            Long orderId = 1L;
            Order existingOrder = OrderFixture.create().withId(orderId).build();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(existingOrder));

            // when
            orderService.deleteOrder(orderId);

            // then
            then(orderRepository).should().delete(existingOrder);
        }
    }
}
