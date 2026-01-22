package com.soundbar91.payment.infrastructure.repository;

import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
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

@SpringBootTest(classes = PaymentJpaRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentJpaRepository 테스트")
class PaymentJpaRepositoryTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.soundbar91.payment.domain.entity")
    @EnableJpaRepositories(basePackages = "com.soundbar91.payment.infrastructure.repository")
    static class TestConfig {}

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("새로운 결제를 저장하면 ID가 생성된다")
        void save_NewPayment_GeneratesId() {
            // given
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);

            // when
            Payment savedPayment = paymentJpaRepository.save(payment);

            // then
            assertThat(savedPayment.getId()).isNotNull();
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(savedPayment.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 결제를 반환한다")
        void findById_WithExistingId_ReturnsPayment() {
            // given
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(30000), PaymentMethod.BANK_TRANSFER);
            Payment persistedPayment = paymentJpaRepository.save(payment);

            // when
            Optional<Payment> found = paymentJpaRepository.findById(persistedPayment.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void findById_WithNonExistingId_ReturnsEmpty() {
            // when
            Optional<Payment> found = paymentJpaRepository.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderId 메서드")
    class FindByOrderId {

        @Test
        @DisplayName("존재하는 주문 ID로 조회하면 결제를 반환한다")
        void findByOrderId_WithExistingOrderId_ReturnsPayment() {
            // given
            Long orderId = 100L;
            Payment payment = new Payment(orderId, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);
            paymentJpaRepository.save(payment);

            // when
            Optional<Payment> found = paymentJpaRepository.findByOrderId(orderId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회하면 빈 Optional을 반환한다")
        void findByOrderId_WithNonExistingOrderId_ReturnsEmpty() {
            // when
            Optional<Payment> found = paymentJpaRepository.findByOrderId(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId 메서드")
    class FindByUserId {

        @Test
        @DisplayName("특정 사용자의 결제만 조회한다")
        void findByUserId_ReturnsUserPayments() {
            // given
            Long userId = 1L;
            Payment payment1 = new Payment(1L, userId, BigDecimal.valueOf(10000), PaymentMethod.CREDIT_CARD);
            Payment payment2 = new Payment(2L, userId, BigDecimal.valueOf(20000), PaymentMethod.DEBIT_CARD);
            Payment otherPayment = new Payment(3L, 2L, BigDecimal.valueOf(30000), PaymentMethod.BANK_TRANSFER);

            paymentJpaRepository.save(payment1);
            paymentJpaRepository.save(payment2);
            paymentJpaRepository.save(otherPayment);

            // when
            List<Payment> userPayments = paymentJpaRepository.findByUserId(userId);

            // then
            assertThat(userPayments).hasSize(2);
            assertThat(userPayments).allMatch(p -> p.getUserId().equals(userId));
        }
    }

    @Nested
    @DisplayName("findByStatus 메서드")
    class FindByStatus {

        @Test
        @DisplayName("특정 상태의 결제만 조회한다")
        void findByStatus_ReturnsPaymentsWithStatus() {
            // given
            Payment pendingPayment1 = new Payment(1L, 1L, BigDecimal.valueOf(10000), PaymentMethod.CREDIT_CARD);
            Payment pendingPayment2 = new Payment(2L, 2L, BigDecimal.valueOf(20000), PaymentMethod.DEBIT_CARD);
            Payment completedPayment = new Payment(3L, 3L, BigDecimal.valueOf(30000), PaymentMethod.BANK_TRANSFER);
            completedPayment.process();
            completedPayment.complete();

            paymentJpaRepository.save(pendingPayment1);
            paymentJpaRepository.save(pendingPayment2);
            paymentJpaRepository.save(completedPayment);

            // when
            List<Payment> pendingPayments = paymentJpaRepository.findByStatus(PaymentStatus.PENDING);
            List<Payment> completedPayments = paymentJpaRepository.findByStatus(PaymentStatus.COMPLETED);

            // then
            assertThat(pendingPayments).hasSize(2);
            assertThat(completedPayments).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("결제를 삭제하면 조회되지 않는다")
        void delete_RemovesPayment() {
            // given
            Payment payment = new Payment(1L, 1L, BigDecimal.valueOf(50000), PaymentMethod.CREDIT_CARD);
            Payment persistedPayment = paymentJpaRepository.save(payment);
            Long paymentId = persistedPayment.getId();

            // when
            paymentJpaRepository.delete(persistedPayment);
            paymentJpaRepository.flush();

            // then
            Optional<Payment> found = paymentJpaRepository.findById(paymentId);
            assertThat(found).isEmpty();
        }
    }
}
