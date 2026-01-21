package com.soundbar91.payment.infrastructure.repository;

import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.repository.PaymentRepository;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PaymentRepository 구현체
 * JPA를 사용한 영속성 계층 구현
 */
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public List<Payment> findAll() {
        return paymentJpaRepository.findAll();
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status);
    }

    @Override
    public void delete(Payment payment) {
        paymentJpaRepository.delete(payment);
    }

    @Override
    public void deleteById(Long id) {
        paymentJpaRepository.deleteById(id);
    }
}
