package com.soundbar91.payment.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.event.PaymentCompletedEvent;
import com.soundbar91.payment.domain.event.PaymentFailedEvent;
import com.soundbar91.payment.domain.event.RefundCompletedEvent;
import com.soundbar91.payment.domain.repository.PaymentRepository;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 서비스
 * 결제 생성, 처리, 환불, 취소 기능 제공
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 결제 생성
     * 주문 생성 시 이벤트를 통해 호출됨
     */
    @Transactional
    public Payment createPayment(Long orderId, Long userId, BigDecimal amount, PaymentMethod paymentMethod) {
        Payment payment = new Payment(orderId, userId, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    /**
     * 결제 조회
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 모든 결제 조회
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * 주문별 결제 조회
     */
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("해당 주문의 결제를 찾을 수 없습니다. Order ID: " + orderId));
    }

    /**
     * 사용자별 결제 내역 조회
     */
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * 상태별 결제 조회
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * 결제 처리
     * PENDING → PROCESSING → COMPLETED
     */
    @Transactional
    public Payment processPayment(Long id) {
        Payment payment = getPaymentById(id);
        payment.process();

        // 결제 처리 시뮬레이션 (실제로는 PG사 API 호출)
        boolean success = simulatePaymentProcessing(payment);

        if (success) {
            payment.complete();
            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getTransactionId()
            ));
        } else {
            String failureReason = "결제 처리 실패: 카드사 승인 거부";
            payment.fail(failureReason);
            eventPublisher.publishEvent(new PaymentFailedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    failureReason
            ));
        }

        return payment;
    }

    /**
     * 환불 처리
     * COMPLETED → REFUNDED
     */
    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);
        payment.refund();

        eventPublisher.publishEvent(new RefundCompletedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount()
        ));

        return payment;
    }

    /**
     * 주문 ID로 환불 처리
     * 주문 취소 이벤트 수신 시 사용
     */
    @Transactional
    public Payment refundPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        if (payment == null) {
            return null;
        }

        if (payment.getStatus().canRefund()) {
            payment.refund();
            eventPublisher.publishEvent(new RefundCompletedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount()
            ));
        } else if (payment.getStatus().canCancel()) {
            payment.cancel();
        }

        return payment;
    }

    /**
     * 결제 취소
     * PENDING, PROCESSING → CANCELLED
     */
    @Transactional
    public Payment cancelPayment(Long id) {
        Payment payment = getPaymentById(id);
        payment.cancel();
        return payment;
    }

    /**
     * 결제 존재 여부 확인
     */
    public boolean existsById(Long id) {
        return paymentRepository.findById(id).isPresent();
    }

    /**
     * 결제 처리 시뮬레이션
     * 실제 구현에서는 PG사 API 호출
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        // 시뮬레이션: 항상 성공
        return true;
    }
}
