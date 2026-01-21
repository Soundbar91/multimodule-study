package com.soundbar91.payment.domain.repository;

import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * 결제 레포지토리 인터페이스
 * 도메인 계층에서 정의하고, infrastructure 계층에서 구현
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    void delete(Payment payment);

    void deleteById(Long id);
}
