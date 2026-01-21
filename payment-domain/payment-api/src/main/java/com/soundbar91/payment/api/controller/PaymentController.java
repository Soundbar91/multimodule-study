package com.soundbar91.payment.api.controller;

import com.soundbar91.payment.api.dto.response.PaymentResponse;
import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import com.soundbar91.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 주문별 결제 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId).stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * 상태별 결제 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status).stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * 결제 처리
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long id) {
        Payment payment = paymentService.processPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 환불 요청
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        Payment payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long id) {
        Payment payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
