package com.soundbar91.payment.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.event.PaymentCompletedEvent;
import com.soundbar91.payment.domain.event.RefundCompletedEvent;
import com.soundbar91.payment.domain.repository.PaymentRepository;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import com.soundbar91.test.fixture.PaymentFixture;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("createPayment 메서드")
    class CreatePayment {

        @Test
        @DisplayName("유효한 정보로 결제를 생성하면 저장된 결제를 반환한다")
        void createPayment_WithValidInfo_ReturnsPayment() {
            // given
            Long orderId = 1L;
            Long userId = 1L;
            BigDecimal amount = BigDecimal.valueOf(50000);
            PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

            Payment expectedPayment = PaymentFixture.create()
                    .withId(1L)
                    .withOrderId(orderId)
                    .withUserId(userId)
                    .withAmount(amount)
                    .withPaymentMethod(paymentMethod)
                    .build();

            given(paymentRepository.save(any(Payment.class))).willReturn(expectedPayment);

            // when
            Payment result = paymentService.createPayment(orderId, userId, amount, paymentMethod);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("getPaymentById 메서드")
    class GetPaymentById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 결제를 반환한다")
        void getPaymentById_WithExistingId_ReturnsPayment() {
            // given
            Long paymentId = 1L;
            Payment payment = PaymentFixture.create().withId(paymentId).build();
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // when
            Payment result = paymentService.getPaymentById(paymentId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void getPaymentById_WithNonExistingId_ThrowsException() {
            // given
            Long paymentId = 999L;
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentById(paymentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("결제를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getPaymentByOrderId 메서드")
    class GetPaymentByOrderId {

        @Test
        @DisplayName("존재하는 주문 ID로 조회하면 결제를 반환한다")
        void getPaymentByOrderId_WithExistingOrderId_ReturnsPayment() {
            // given
            Long orderId = 1L;
            Payment payment = PaymentFixture.create().withOrderId(orderId).build();
            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

            // when
            Payment result = paymentService.getPaymentByOrderId(orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회하면 예외가 발생한다")
        void getPaymentByOrderId_WithNonExistingOrderId_ThrowsException() {
            // given
            Long orderId = 999L;
            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPaymentByOrderId(orderId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("해당 주문의 결제를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getPaymentsByUserId 메서드")
    class GetPaymentsByUserId {

        @Test
        @DisplayName("사용자별 결제 목록을 반환한다")
        void getPaymentsByUserId_ReturnsUserPayments() {
            // given
            Long userId = 1L;
            List<Payment> payments = List.of(
                    PaymentFixture.create().withId(1L).withUserId(userId).build(),
                    PaymentFixture.create().withId(2L).withUserId(userId).build()
            );
            given(paymentRepository.findByUserId(userId)).willReturn(payments);

            // when
            List<Payment> result = paymentService.getPaymentsByUserId(userId);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getPaymentsByStatus 메서드")
    class GetPaymentsByStatus {

        @Test
        @DisplayName("상태별 결제 목록을 반환한다")
        void getPaymentsByStatus_ReturnsPaymentsWithStatus() {
            // given
            PaymentStatus status = PaymentStatus.PENDING;
            List<Payment> pendingPayments = List.of(
                    PaymentFixture.create().withId(1L).withStatus(status).build(),
                    PaymentFixture.create().withId(2L).withStatus(status).build()
            );
            given(paymentRepository.findByStatus(status)).willReturn(pendingPayments);

            // when
            List<Payment> result = paymentService.getPaymentsByStatus(status);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("processPayment 메서드")
    class ProcessPayment {

        @Test
        @DisplayName("대기 중인 결제를 처리하면 완료 상태가 된다")
        void processPayment_WithPendingPayment_BecomesCompleted() {
            // given
            Long paymentId = 1L;
            Payment pendingPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withOrderId(1L)
                    .withUserId(1L)
                    .withAmount(BigDecimal.valueOf(50000))
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(pendingPayment));

            // when
            Payment result = paymentService.processPayment(paymentId);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.getTransactionId()).isNotNull();
            then(eventPublisher).should().publishEvent(any(PaymentCompletedEvent.class));
        }

        @Test
        @DisplayName("결제 처리 시 PaymentCompletedEvent가 발행된다")
        void processPayment_PublishesPaymentCompletedEvent() {
            // given
            Long paymentId = 1L;
            Payment pendingPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withOrderId(2L)
                    .withUserId(3L)
                    .withAmount(BigDecimal.valueOf(30000))
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(pendingPayment));

            ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);

            // when
            paymentService.processPayment(paymentId);

            // then
            then(eventPublisher).should().publishEvent(eventCaptor.capture());
            PaymentCompletedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getPaymentId()).isEqualTo(1L);
            assertThat(capturedEvent.getOrderId()).isEqualTo(2L);
            assertThat(capturedEvent.getUserId()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("refundPayment 메서드")
    class RefundPayment {

        @Test
        @DisplayName("완료된 결제를 환불하면 환불 상태가 된다")
        void refundPayment_WithCompletedPayment_BecomesRefunded() {
            // given
            Long paymentId = 1L;
            Payment completedPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withOrderId(1L)
                    .withUserId(1L)
                    .withAmount(BigDecimal.valueOf(50000))
                    .withStatus(PaymentStatus.COMPLETED)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(completedPayment));

            // when
            Payment result = paymentService.refundPayment(paymentId);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            then(eventPublisher).should().publishEvent(any(RefundCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("refundPaymentByOrderId 메서드")
    class RefundPaymentByOrderId {

        @Test
        @DisplayName("완료된 결제를 주문 ID로 환불하면 환불 상태가 된다")
        void refundPaymentByOrderId_WithCompletedPayment_BecomesRefunded() {
            // given
            Long orderId = 1L;
            Payment completedPayment = PaymentFixture.create()
                    .withId(1L)
                    .withOrderId(orderId)
                    .withUserId(1L)
                    .withStatus(PaymentStatus.COMPLETED)
                    .build();

            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(completedPayment));

            // when
            Payment result = paymentService.refundPaymentByOrderId(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("대기 중인 결제를 주문 ID로 처리하면 취소 상태가 된다")
        void refundPaymentByOrderId_WithPendingPayment_BecomesCancelled() {
            // given
            Long orderId = 1L;
            Payment pendingPayment = PaymentFixture.create()
                    .withId(1L)
                    .withOrderId(orderId)
                    .withStatus(PaymentStatus.PENDING)
                    .build();

            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(pendingPayment));

            // when
            Payment result = paymentService.refundPaymentByOrderId(orderId);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("결제가 없으면 null을 반환한다")
        void refundPaymentByOrderId_WithNoPayment_ReturnsNull() {
            // given
            Long orderId = 999L;
            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            // when
            Payment result = paymentService.refundPaymentByOrderId(orderId);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("cancelPayment 메서드")
    class CancelPayment {

        @Test
        @DisplayName("대기 중인 결제를 취소하면 취소 상태가 된다")
        void cancelPayment_WithPendingPayment_BecomesCancelled() {
            // given
            Long paymentId = 1L;
            Payment pendingPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .build();

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(pendingPayment));

            // when
            Payment result = paymentService.cancelPayment(paymentId);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("existsById 메서드")
    class ExistsById {

        @Test
        @DisplayName("존재하는 결제 ID이면 true를 반환한다")
        void existsById_WithExistingPayment_ReturnsTrue() {
            // given
            Long paymentId = 1L;
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(PaymentFixture.createDefault()));

            // when
            boolean result = paymentService.existsById(paymentId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID이면 false를 반환한다")
        void existsById_WithNonExistingPayment_ReturnsFalse() {
            // given
            Long paymentId = 999L;
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // when
            boolean result = paymentService.existsById(paymentId);

            // then
            assertThat(result).isFalse();
        }
    }
}
