package com.soundbar91.payment.domain.entity;

import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import com.soundbar91.test.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment 엔티티 상태 전이 테스트")
class PaymentTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("결제 생성 시 기본 상태는 PENDING이다")
        void newPayment_HasPendingStatus() {
            // given & when
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제 생성 시 생성 시간이 설정된다")
        void newPayment_HasCreatedAt() {
            // given & when
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);

            // then
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 생성 시 transactionId는 null이다")
        void newPayment_HasNullTransactionId() {
            // given & when
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);

            // then
            assertThat(payment.getTransactionId()).isNull();
        }
    }

    @Nested
    @DisplayName("process 메서드")
    class Process {

        @Test
        @DisplayName("PENDING 상태에서 처리하면 PROCESSING 상태가 된다")
        void process_FromPending_BecomesProcessing() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when
            payment.process();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("처리 시 transactionId가 생성된다")
        void process_GeneratesTransactionId() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when
            payment.process();

            // then
            assertThat(payment.getTransactionId()).isNotNull();
            assertThat(payment.getTransactionId()).startsWith("TXN-");
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 처리하면 예외가 발생한다")
        void process_FromNonPending_ThrowsException() {
            // given
            Payment payment = PaymentFixture.createProcessingPayment();

            // when & then
            assertThatThrownBy(payment::process)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("처리할 수 없는 결제 상태입니다");
        }
    }

    @Nested
    @DisplayName("complete 메서드")
    class Complete {

        @Test
        @DisplayName("PROCESSING 상태에서 완료하면 COMPLETED 상태가 된다")
        void complete_FromProcessing_BecomesCompleted() {
            // given
            Payment payment = PaymentFixture.createProcessingPayment();

            // when
            payment.complete();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("완료 시 completedAt이 설정된다")
        void complete_SetsCompletedAt() {
            // given
            Payment payment = PaymentFixture.createProcessingPayment();

            // when
            payment.complete();

            // then
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING이 아닌 상태에서 완료하면 예외가 발생한다")
        void complete_FromNonProcessing_ThrowsException() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when & then
            assertThatThrownBy(payment::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("처리 중인 결제만 완료할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("fail 메서드")
    class Fail {

        @Test
        @DisplayName("PROCESSING 상태에서 실패하면 FAILED 상태가 된다")
        void fail_FromProcessing_BecomesFailed() {
            // given
            Payment payment = PaymentFixture.createProcessingPayment();
            String reason = "카드 승인 거부";

            // when
            payment.fail(reason);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("PROCESSING이 아닌 상태에서 실패 처리하면 예외가 발생한다")
        void fail_FromNonProcessing_ThrowsException() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when & then
            assertThatThrownBy(() -> payment.fail("실패 사유"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("처리 중인 결제만 실패 처리할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("refund 메서드")
    class Refund {

        @Test
        @DisplayName("COMPLETED 상태에서 환불하면 REFUNDED 상태가 된다")
        void refund_FromCompleted_BecomesRefunded() {
            // given
            Payment payment = PaymentFixture.createCompletedPayment();

            // when
            payment.refund();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("환불 시 refundedAt이 설정된다")
        void refund_SetsRefundedAt() {
            // given
            Payment payment = PaymentFixture.createCompletedPayment();

            // when
            payment.refund();

            // then
            assertThat(payment.getRefundedAt()).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED가 아닌 상태에서 환불하면 예외가 발생한다")
        void refund_FromNonCompleted_ThrowsException() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when & then
            assertThatThrownBy(payment::refund)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("환불할 수 없는 결제 상태입니다");
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("PENDING 상태에서 취소하면 CANCELLED 상태가 된다")
        void cancel_FromPending_BecomesCancelled() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when
            payment.cancel();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 취소하면 CANCELLED 상태가 된다")
        void cancel_FromProcessing_BecomesCancelled() {
            // given
            Payment payment = PaymentFixture.createProcessingPayment();

            // when
            payment.cancel();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 취소하면 예외가 발생한다")
        void cancel_FromCompleted_ThrowsException() {
            // given
            Payment payment = PaymentFixture.createCompletedPayment();

            // when & then
            assertThatThrownBy(payment::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("취소할 수 없는 결제 상태입니다");
        }
    }

    @Nested
    @DisplayName("전체 결제 생애주기")
    class PaymentLifecycle {

        @Test
        @DisplayName("PENDING → PROCESSING → COMPLETED 순서로 상태가 전이된다")
        void successfulPayment_TransitionsCorrectly() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when & then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            payment.process();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
            assertThat(payment.getTransactionId()).isNotNull();

            payment.complete();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING → PROCESSING → COMPLETED → REFUNDED 순서로 환불이 완료된다")
        void refundedPayment_TransitionsCorrectly() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when
            payment.process();
            payment.complete();
            payment.refund();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING → PROCESSING → FAILED 순서로 결제 실패가 처리된다")
        void failedPayment_TransitionsCorrectly() {
            // given
            Payment payment = PaymentFixture.createDefault();

            // when
            payment.process();
            payment.fail("잔액 부족");

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("잔액 부족");
        }
    }
}
