package com.soundbar91.payment.service.listener;

import com.soundbar91.order.domain.event.OrderCancelledEvent;
import com.soundbar91.order.domain.event.OrderCreatedEvent;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Order 도메인 이벤트 리스너
 * 이벤트 기반 통신을 통한 도메인 간 느슨한 결합
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final PaymentService paymentService;

    public OrderEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 주문 생성 이벤트 처리
     * 주문이 생성되면 결제 정보를 자동으로 생성
     */
    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신: {}", event);

        // 결제 생성 (기본 결제 수단: 신용카드)
        paymentService.createPayment(
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                PaymentMethod.CREDIT_CARD
        );

        log.info("주문 ID {}에 대한 결제 정보 생성 완료", event.getOrderId());
    }

    /**
     * 주문 취소 이벤트 처리
     * 주문이 취소되면 결제를 환불 또는 취소 처리
     */
    @EventListener
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("주문 취소 이벤트 수신: {}", event);

        paymentService.refundPaymentByOrderId(event.getOrderId());

        log.info("주문 ID {}에 대한 결제 환불/취소 처리 완료", event.getOrderId());
    }
}
