package com.soundbar91.test.fixture;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Order 엔티티 테스트 픽스처
 */
public class OrderFixture {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long DEFAULT_SHOP_ID = 1L;
    private static final String DEFAULT_PRODUCT_NAME = "테스트 상품";
    private static final Integer DEFAULT_QUANTITY = 2;
    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = BigDecimal.valueOf(50000);
    private static final String DEFAULT_DELIVERY_ADDRESS = "서울시 강남구 배송로 123";

    private Long userId = DEFAULT_USER_ID;
    private Long shopId = DEFAULT_SHOP_ID;
    private String productName = DEFAULT_PRODUCT_NAME;
    private Integer quantity = DEFAULT_QUANTITY;
    private BigDecimal totalAmount = DEFAULT_TOTAL_AMOUNT;
    private String deliveryAddress = DEFAULT_DELIVERY_ADDRESS;
    private Long id = null;
    private OrderStatus status = null;

    private OrderFixture() {}

    public static OrderFixture create() {
        return new OrderFixture();
    }

    public static Order createDefault() {
        return new Order(DEFAULT_USER_ID, DEFAULT_SHOP_ID, DEFAULT_PRODUCT_NAME,
                        DEFAULT_QUANTITY, DEFAULT_TOTAL_AMOUNT, DEFAULT_DELIVERY_ADDRESS);
    }

    public static Order createConfirmedOrder() {
        Order order = createDefault();
        order.confirm();
        return order;
    }

    public static Order createShippedOrder() {
        Order order = createDefault();
        order.confirm();
        order.ship();
        return order;
    }

    public static Order createDeliveredOrder() {
        Order order = createDefault();
        order.confirm();
        order.ship();
        order.deliver();
        return order;
    }

    public static Order createCancelledOrder() {
        Order order = createDefault();
        order.cancel();
        return order;
    }

    public OrderFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public OrderFixture withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public OrderFixture withShopId(Long shopId) {
        this.shopId = shopId;
        return this;
    }

    public OrderFixture withProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public OrderFixture withQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderFixture withTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public OrderFixture withDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        return this;
    }

    public OrderFixture withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public Order build() {
        Order order = new Order(userId, shopId, productName, quantity, totalAmount, deliveryAddress);
        if (id != null) {
            setField(order, "id", id);
        }
        if (status != null) {
            setField(order, "status", status);
        }
        return order;
    }

    /**
     * 리플렉션을 사용하여 필드 설정 (테스트 전용)
     */
    private static void setField(Order order, String fieldName, Object value) {
        try {
            Field field = Order.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(order, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field via reflection: " + fieldName, e);
        }
    }
}
