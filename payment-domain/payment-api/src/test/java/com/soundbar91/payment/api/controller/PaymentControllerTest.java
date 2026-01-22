package com.soundbar91.payment.api.controller;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentStatus;
import com.soundbar91.payment.service.PaymentService;
import com.soundbar91.test.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentControllerTest.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

    @EnableAutoConfiguration
    @ComponentScan(basePackages = "com.soundbar91.payment.api")
    static class TestConfig {

        @RestControllerAdvice
        static class TestExceptionHandler {
            @ExceptionHandler(NotFoundException.class)
            public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Nested
    @DisplayName("GET /api/v2/payments/{id}")
    class GetPayment {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200 OK와 결제 정보를 반환한다")
        void getPayment_WithExistingId_Returns200() throws Exception {
            // given
            Long paymentId = 1L;
            Payment payment = PaymentFixture.create()
                    .withId(paymentId)
                    .withAmount(BigDecimal.valueOf(50000))
                    .build();

            given(paymentService.getPaymentById(paymentId)).willReturn(payment);

            // when & then
            mockMvc.perform(get("/api/v2/payments/{id}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(paymentId))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404 Not Found를 반환한다")
        void getPayment_WithNonExistingId_Returns404() throws Exception {
            // given
            Long paymentId = 999L;
            given(paymentService.getPaymentById(paymentId))
                    .willThrow(new NotFoundException("결제를 찾을 수 없습니다. ID: " + paymentId));

            // when & then
            mockMvc.perform(get("/api/v2/payments/{id}", paymentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/payments/order/{orderId}")
    class GetPaymentByOrderId {

        @Test
        @DisplayName("존재하는 주문 ID로 조회하면 200 OK와 결제 정보를 반환한다")
        void getPaymentByOrderId_WithExistingOrderId_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Payment payment = PaymentFixture.create()
                    .withId(1L)
                    .withOrderId(orderId)
                    .build();

            given(paymentService.getPaymentByOrderId(orderId)).willReturn(payment);

            // when & then
            mockMvc.perform(get("/api/v2/payments/order/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/payments/user/{userId}")
    class GetPaymentsByUserId {

        @Test
        @DisplayName("사용자별 결제를 조회하면 해당 사용자의 결제 목록을 반환한다")
        void getPaymentsByUserId_Returns200WithList() throws Exception {
            // given
            Long userId = 1L;
            List<Payment> payments = List.of(
                    PaymentFixture.create().withId(1L).withUserId(userId).build(),
                    PaymentFixture.create().withId(2L).withUserId(userId).build()
            );

            given(paymentService.getPaymentsByUserId(userId)).willReturn(payments);

            // when & then
            mockMvc.perform(get("/api/v2/payments/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/payments/status/{status}")
    class GetPaymentsByStatus {

        @Test
        @DisplayName("상태별 결제를 조회하면 해당 상태의 결제 목록을 반환한다")
        void getPaymentsByStatus_Returns200WithList() throws Exception {
            // given
            List<Payment> payments = List.of(
                    PaymentFixture.create().withId(1L).withStatus(PaymentStatus.PENDING).build(),
                    PaymentFixture.create().withId(2L).withStatus(PaymentStatus.PENDING).build()
            );

            given(paymentService.getPaymentsByStatus(PaymentStatus.PENDING)).willReturn(payments);

            // when & then
            mockMvc.perform(get("/api/v2/payments/status/{status}", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/payments/{id}/process")
    class ProcessPayment {

        @Test
        @DisplayName("결제를 처리하면 200 OK를 반환한다")
        void processPayment_Returns200() throws Exception {
            // given
            Long paymentId = 1L;
            Payment processedPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withStatus(PaymentStatus.COMPLETED)
                    .withTransactionId("TXN-12345678")
                    .build();

            given(paymentService.processPayment(paymentId)).willReturn(processedPayment);

            // when & then
            mockMvc.perform(post("/api/v2/payments/{id}/process", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.transactionId").value("TXN-12345678"));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/payments/{id}/refund")
    class RefundPayment {

        @Test
        @DisplayName("결제를 환불하면 200 OK를 반환한다")
        void refundPayment_Returns200() throws Exception {
            // given
            Long paymentId = 1L;
            Payment refundedPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withStatus(PaymentStatus.REFUNDED)
                    .build();

            given(paymentService.refundPayment(paymentId)).willReturn(refundedPayment);

            // when & then
            mockMvc.perform(post("/api/v2/payments/{id}/refund", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/payments/{id}/cancel")
    class CancelPayment {

        @Test
        @DisplayName("결제를 취소하면 200 OK를 반환한다")
        void cancelPayment_Returns200() throws Exception {
            // given
            Long paymentId = 1L;
            Payment cancelledPayment = PaymentFixture.create()
                    .withId(paymentId)
                    .withStatus(PaymentStatus.CANCELLED)
                    .build();

            given(paymentService.cancelPayment(paymentId)).willReturn(cancelledPayment);

            // when & then
            mockMvc.perform(post("/api/v2/payments/{id}/cancel", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }
}
