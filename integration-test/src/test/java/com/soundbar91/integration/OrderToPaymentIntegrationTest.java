package com.soundbar91.integration;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.order.service.OrderService;
import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import com.soundbar91.payment.service.PaymentService;
import com.soundbar91.shop.domain.entity.Shop;
import com.soundbar91.shop.domain.vo.ShopCategory;
import com.soundbar91.shop.service.ShopService;
import com.soundbar91.user.domain.entity.User;
import com.soundbar91.user.domain.vo.UserRole;
import com.soundbar91.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("주문-결제 도메인 간 통합 테스트")
class OrderToPaymentIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    private User testUser;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = userService.createUser(
                "테스트 구매자",
                "buyer" + System.currentTimeMillis() + "@example.com",
                "010-1234-5678",
                UserRole.USER
        );

        // 테스트 상점 생성
        testShop = shopService.createShop(
                "테스트 상점",
                ShopCategory.RESTAURANT,
                "테스트 상점 설명",
                "서울시 강남구",
                "02-1234-5678",
                testUser.getId()
        );
    }

    @Nested
    @DisplayName("주문 생성 및 결제 생애주기")
    class OrderPaymentLifecycle {

        @Test
        @DisplayName("주문을 생성하면 결제를 생성할 수 있다")
        void createOrderAndPayment() {
            // given
            BigDecimal amount = BigDecimal.valueOf(50000);

            // when
            Order order = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "테스트 상품",
                    2,
                    amount,
                    "서울시 강남구 배송지"
            );

            Payment payment = paymentService.createPayment(
                    order.getId(),
                    testUser.getId(),
                    amount,
                    PaymentMethod.CREDIT_CARD
            );

            // then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(order.getId());
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제를 처리하면 완료 상태가 된다")
        void processPayment() {
            // given
            Order order = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "결제 처리 테스트 상품",
                    1,
                    BigDecimal.valueOf(30000),
                    "배송지"
            );

            Payment payment = paymentService.createPayment(
                    order.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(30000),
                    PaymentMethod.CREDIT_CARD
            );

            // when
            Payment processedPayment = paymentService.processPayment(payment.getId());

            // then
            assertThat(processedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(processedPayment.getTransactionId()).isNotNull();
            assertThat(processedPayment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 생애주기를 완전히 진행할 수 있다")
        void fullOrderLifecycle() {
            // given
            Order order = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "생애주기 테스트 상품",
                    1,
                    BigDecimal.valueOf(45000),
                    "서울시 서초구"
            );

            Payment payment = paymentService.createPayment(
                    order.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(45000),
                    PaymentMethod.BANK_TRANSFER
            );

            // when & then
            // 결제 처리
            Payment processedPayment = paymentService.processPayment(payment.getId());
            assertThat(processedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            // 주문 확정
            Order confirmedOrder = orderService.confirmOrder(order.getId());
            assertThat(confirmedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // 배송 시작
            Order shippedOrder = orderService.shipOrder(order.getId());
            assertThat(shippedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);

            // 배송 완료
            Order deliveredOrder = orderService.deliverOrder(order.getId());
            assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }

    @Nested
    @DisplayName("주문 취소 및 환불 시나리오")
    class OrderCancellationAndRefund {

        @Test
        @DisplayName("대기 중인 주문을 취소하면 결제도 취소된다")
        void cancelPendingOrder() {
            // given
            Order order = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "취소 테스트 상품",
                    1,
                    BigDecimal.valueOf(20000),
                    "배송지"
            );

            Payment payment = paymentService.createPayment(
                    order.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(20000),
                    PaymentMethod.CREDIT_CARD
            );

            // when
            Order cancelledOrder = orderService.cancelOrder(order.getId());
            Payment cancelledPayment = paymentService.refundPaymentByOrderId(order.getId());

            // then
            assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("결제 완료된 주문을 취소하면 환불 처리된다")
        void cancelCompletedPaymentOrder() {
            // given
            Order order = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "환불 테스트 상품",
                    1,
                    BigDecimal.valueOf(35000),
                    "배송지"
            );

            Payment payment = paymentService.createPayment(
                    order.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(35000),
                    PaymentMethod.DEBIT_CARD
            );

            // 결제 완료 처리
            paymentService.processPayment(payment.getId());

            // when
            Order cancelledOrder = orderService.cancelOrder(order.getId());
            Payment refundedPayment = paymentService.refundPaymentByOrderId(order.getId());

            // then
            assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(refundedPayment.getRefundedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("사용자별 주문 및 결제 조회")
    class UserOrdersAndPayments {

        @Test
        @DisplayName("사용자의 모든 주문과 결제 내역을 조회할 수 있다")
        void getUserOrdersAndPayments() {
            // given
            Order order1 = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "상품1",
                    1,
                    BigDecimal.valueOf(10000),
                    "배송지1"
            );
            paymentService.createPayment(
                    order1.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(10000),
                    PaymentMethod.CREDIT_CARD
            );

            Order order2 = orderService.createOrder(
                    testUser.getId(),
                    testShop.getId(),
                    "상품2",
                    2,
                    BigDecimal.valueOf(25000),
                    "배송지2"
            );
            paymentService.createPayment(
                    order2.getId(),
                    testUser.getId(),
                    BigDecimal.valueOf(25000),
                    PaymentMethod.BANK_TRANSFER
            );

            // when
            var userOrders = orderService.getOrdersByUserId(testUser.getId());
            var userPayments = paymentService.getPaymentsByUserId(testUser.getId());

            // then
            assertThat(userOrders).hasSizeGreaterThanOrEqualTo(2);
            assertThat(userPayments).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
