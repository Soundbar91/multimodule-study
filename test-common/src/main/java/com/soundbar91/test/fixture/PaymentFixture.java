package com.soundbar91.test.fixture;

import com.soundbar91.payment.domain.entity.Payment;
import com.soundbar91.payment.domain.vo.PaymentMethod;
import com.soundbar91.payment.domain.vo.PaymentStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Payment 엔티티 테스트 픽스처
 */
public class PaymentFixture {

    private static final Long DEFAULT_ORDER_ID = 1L;
    private static final Long DEFAULT_USER_ID = 1L;
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(50000);
    private static final PaymentMethod DEFAULT_PAYMENT_METHOD = PaymentMethod.CREDIT_CARD;

    private Long orderId = DEFAULT_ORDER_ID;
    private Long userId = DEFAULT_USER_ID;
    private BigDecimal amount = DEFAULT_AMOUNT;
    private PaymentMethod paymentMethod = DEFAULT_PAYMENT_METHOD;
    private Long id = null;
    private PaymentStatus status = null;
    private String transactionId = null;
    private String failureReason = null;

    private PaymentFixture() {}

    public static PaymentFixture create() {
        return new PaymentFixture();
    }

    public static Payment createDefault() {
        return new Payment(DEFAULT_ORDER_ID, DEFAULT_USER_ID, DEFAULT_AMOUNT, DEFAULT_PAYMENT_METHOD);
    }

    public static Payment createProcessingPayment() {
        Payment payment = createDefault();
        payment.process();
        return payment;
    }

    public static Payment createCompletedPayment() {
        Payment payment = createDefault();
        payment.process();
        payment.complete();
        return payment;
    }

    public static Payment createFailedPayment(String reason) {
        Payment payment = createDefault();
        payment.process();
        payment.fail(reason);
        return payment;
    }

    public static Payment createRefundedPayment() {
        Payment payment = createDefault();
        payment.process();
        payment.complete();
        payment.refund();
        return payment;
    }

    public static Payment createCancelledPayment() {
        Payment payment = createDefault();
        payment.cancel();
        return payment;
    }

    public static Payment createWithBankTransfer() {
        return new Payment(DEFAULT_ORDER_ID, DEFAULT_USER_ID, DEFAULT_AMOUNT, PaymentMethod.BANK_TRANSFER);
    }

    public static Payment createWithDebitCard() {
        return new Payment(DEFAULT_ORDER_ID, DEFAULT_USER_ID, DEFAULT_AMOUNT, PaymentMethod.DEBIT_CARD);
    }

    public PaymentFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public PaymentFixture withOrderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public PaymentFixture withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public PaymentFixture withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentFixture withPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentFixture withStatus(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentFixture withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public PaymentFixture withFailureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public Payment build() {
        Payment payment = new Payment(orderId, userId, amount, paymentMethod);
        if (id != null) {
            setField(payment, "id", id);
        }
        if (status != null) {
            setField(payment, "status", status);
        }
        if (transactionId != null) {
            setField(payment, "transactionId", transactionId);
        }
        if (failureReason != null) {
            setField(payment, "failureReason", failureReason);
        }
        return payment;
    }

    /**
     * 리플렉션을 사용하여 필드 설정 (테스트 전용)
     */
    private static void setField(Payment payment, String fieldName, Object value) {
        try {
            Field field = Payment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(payment, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field via reflection: " + fieldName, e);
        }
    }
}
