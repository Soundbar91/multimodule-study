package com.soundbar91.order.domain.entity;

import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.test.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 엔티티 상태 전이 테스트")
class OrderTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("주문 생성 시 기본 상태는 PENDING이다")
        void newOrder_HasPendingStatus() {
            // given & when
            Order order = new Order(1L, 1L, "상품", 2, BigDecimal.valueOf(50000), "주소");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("주문 생성 시 생성 시간이 설정된다")
        void newOrder_HasCreatedAt() {
            // given & when
            Order order = new Order(1L, 1L, "상품", 2, BigDecimal.valueOf(50000), "주소");

            // then
            assertThat(order.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("confirm 메서드")
    class Confirm {

        @Test
        @DisplayName("PENDING 상태에서 확정하면 CONFIRMED 상태가 된다")
        void confirm_FromPending_BecomesConfirmed() {
            // given
            Order order = OrderFixture.createDefault();

            // when
            order.confirm();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 확정하면 예외가 발생한다")
        void confirm_FromNonPending_ThrowsException() {
            // given
            Order order = OrderFixture.createConfirmedOrder();

            // when & then
            assertThatThrownBy(order::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("대기 중인 주문만 확정할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("ship 메서드")
    class Ship {

        @Test
        @DisplayName("CONFIRMED 상태에서 배송하면 SHIPPED 상태가 된다")
        void ship_FromConfirmed_BecomesShipped() {
            // given
            Order order = OrderFixture.createConfirmedOrder();

            // when
            order.ship();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("PENDING 상태에서 배송하면 예외가 발생한다")
        void ship_FromPending_ThrowsException() {
            // given
            Order order = OrderFixture.createDefault();

            // when & then
            assertThatThrownBy(order::ship)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("확정되거나 준비 중인 주문만 배송할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("deliver 메서드")
    class Deliver {

        @Test
        @DisplayName("SHIPPED 상태에서 배송 완료하면 DELIVERED 상태가 된다")
        void deliver_FromShipped_BecomesDelivered() {
            // given
            Order order = OrderFixture.createShippedOrder();

            // when
            order.deliver();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("SHIPPED가 아닌 상태에서 배송 완료하면 예외가 발생한다")
        void deliver_FromNonShipped_ThrowsException() {
            // given
            Order order = OrderFixture.createConfirmedOrder();

            // when & then
            assertThatThrownBy(order::deliver)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("배송 중인 주문만 배송 완료할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("PENDING 상태에서 취소하면 CANCELLED 상태가 된다")
        void cancel_FromPending_BecomesCancelled() {
            // given
            Order order = OrderFixture.createDefault();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("CONFIRMED 상태에서 취소하면 CANCELLED 상태가 된다")
        void cancel_FromConfirmed_BecomesCancelled() {
            // given
            Order order = OrderFixture.createConfirmedOrder();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("SHIPPED 상태에서 취소하면 예외가 발생한다")
        void cancel_FromShipped_ThrowsException() {
            // given
            Order order = OrderFixture.createShippedOrder();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("취소할 수 없는 주문 상태입니다");
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소하면 예외가 발생한다")
        void cancel_FromDelivered_ThrowsException() {
            // given
            Order order = OrderFixture.createDeliveredOrder();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("취소할 수 없는 주문 상태입니다");
        }

        @Test
        @DisplayName("CANCELLED 상태에서 취소하면 예외가 발생한다")
        void cancel_FromCancelled_ThrowsException() {
            // given
            Order order = OrderFixture.createCancelledOrder();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("취소할 수 없는 주문 상태입니다");
        }
    }

    @Nested
    @DisplayName("전체 주문 생애주기")
    class OrderLifecycle {

        @Test
        @DisplayName("PENDING → CONFIRMED → SHIPPED → DELIVERED 순서로 상태가 전이된다")
        void fullLifecycle_TransitionsCorrectly() {
            // given
            Order order = OrderFixture.createDefault();

            // when & then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            order.confirm();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            order.ship();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            order.deliver();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }
}
