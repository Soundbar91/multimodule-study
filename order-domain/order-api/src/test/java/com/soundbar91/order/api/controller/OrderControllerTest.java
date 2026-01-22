package com.soundbar91.order.api.controller;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.order.api.dto.request.CreateOrderRequest;
import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.order.service.OrderService;
import com.soundbar91.test.fixture.OrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = OrderControllerTest.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OrderController 테스트")
class OrderControllerTest {

    @EnableAutoConfiguration
    @ComponentScan(basePackages = "com.soundbar91.order.api")
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Nested
    @DisplayName("POST /api/v2/orders")
    class CreateOrder {

        @Test
        @DisplayName("유효한 요청으로 주문을 생성하면 201 Created를 반환한다")
        void createOrder_WithValidRequest_Returns201() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1L, 1L, "테스트 상품", 2, BigDecimal.valueOf(50000), "서울시 강남구");

            Order createdOrder = OrderFixture.create()
                    .withId(1L)
                    .withUserId(1L)
                    .withShopId(1L)
                    .withProductName("테스트 상품")
                    .build();

            given(orderService.createOrder(any(), any(), any(), any(), any(), any())).willReturn(createdOrder);

            // when & then
            mockMvc.perform(post("/api/v2/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.productName").value("테스트 상품"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200 OK와 주문 정보를 반환한다")
        void getOrder_WithExistingId_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Order order = OrderFixture.create()
                    .withId(orderId)
                    .withProductName("조회 테스트 상품")
                    .build();

            given(orderService.getOrderById(orderId)).willReturn(order);

            // when & then
            mockMvc.perform(get("/api/v2/orders/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.productName").value("조회 테스트 상품"));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404 Not Found를 반환한다")
        void getOrder_WithNonExistingId_Returns404() throws Exception {
            // given
            Long orderId = 999L;
            given(orderService.getOrderById(orderId))
                    .willThrow(new NotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));

            // when & then
            mockMvc.perform(get("/api/v2/orders/{id}", orderId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/orders")
    class GetAllOrders {

        @Test
        @DisplayName("모든 주문을 조회하면 200 OK와 주문 목록을 반환한다")
        void getAllOrders_Returns200WithList() throws Exception {
            // given
            List<Order> orders = List.of(
                    OrderFixture.create().withId(1L).build(),
                    OrderFixture.create().withId(2L).build()
            );

            given(orderService.getAllOrders()).willReturn(orders);

            // when & then
            mockMvc.perform(get("/api/v2/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/orders/user/{userId}")
    class GetOrdersByUserId {

        @Test
        @DisplayName("사용자별 주문을 조회하면 해당 사용자의 주문 목록을 반환한다")
        void getOrdersByUserId_Returns200WithList() throws Exception {
            // given
            Long userId = 1L;
            List<Order> orders = List.of(
                    OrderFixture.create().withId(1L).withUserId(userId).build(),
                    OrderFixture.create().withId(2L).withUserId(userId).build()
            );

            given(orderService.getOrdersByUserId(userId)).willReturn(orders);

            // when & then
            mockMvc.perform(get("/api/v2/orders/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/orders/status/{status}")
    class GetOrdersByStatus {

        @Test
        @DisplayName("상태별 주문을 조회하면 해당 상태의 주문 목록을 반환한다")
        void getOrdersByStatus_Returns200WithList() throws Exception {
            // given
            List<Order> orders = List.of(
                    OrderFixture.create().withId(1L).withStatus(OrderStatus.PENDING).build(),
                    OrderFixture.create().withId(2L).withStatus(OrderStatus.PENDING).build()
            );

            given(orderService.getOrdersByStatus(OrderStatus.PENDING)).willReturn(orders);

            // when & then
            mockMvc.perform(get("/api/v2/orders/status/{status}", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/orders/{id}/confirm")
    class ConfirmOrder {

        @Test
        @DisplayName("주문을 확정하면 200 OK를 반환한다")
        void confirmOrder_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Order confirmedOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.CONFIRMED)
                    .build();

            given(orderService.confirmOrder(orderId)).willReturn(confirmedOrder);

            // when & then
            mockMvc.perform(patch("/api/v2/orders/{id}/confirm", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/orders/{id}/ship")
    class ShipOrder {

        @Test
        @DisplayName("주문을 배송 처리하면 200 OK를 반환한다")
        void shipOrder_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Order shippedOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.SHIPPED)
                    .build();

            given(orderService.shipOrder(orderId)).willReturn(shippedOrder);

            // when & then
            mockMvc.perform(patch("/api/v2/orders/{id}/ship", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SHIPPED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/orders/{id}/deliver")
    class DeliverOrder {

        @Test
        @DisplayName("주문을 배송 완료 처리하면 200 OK를 반환한다")
        void deliverOrder_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Order deliveredOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.DELIVERED)
                    .build();

            given(orderService.deliverOrder(orderId)).willReturn(deliveredOrder);

            // when & then
            mockMvc.perform(patch("/api/v2/orders/{id}/deliver", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELIVERED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/orders/{id}/cancel")
    class CancelOrder {

        @Test
        @DisplayName("주문을 취소하면 200 OK를 반환한다")
        void cancelOrder_Returns200() throws Exception {
            // given
            Long orderId = 1L;
            Order cancelledOrder = OrderFixture.create()
                    .withId(orderId)
                    .withStatus(OrderStatus.CANCELLED)
                    .build();

            given(orderService.cancelOrder(orderId)).willReturn(cancelledOrder);

            // when & then
            mockMvc.perform(patch("/api/v2/orders/{id}/cancel", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/orders/{id}")
    class DeleteOrder {

        @Test
        @DisplayName("존재하는 주문을 삭제하면 204 No Content를 반환한다")
        void deleteOrder_WithExistingOrder_Returns204() throws Exception {
            // given
            Long orderId = 1L;
            willDoNothing().given(orderService).deleteOrder(orderId);

            // when & then
            mockMvc.perform(delete("/api/v2/orders/{id}", orderId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 주문을 삭제하면 404 Not Found를 반환한다")
        void deleteOrder_WithNonExistingOrder_Returns404() throws Exception {
            // given
            Long orderId = 999L;
            willThrow(new NotFoundException("주문을 찾을 수 없습니다"))
                    .given(orderService).deleteOrder(orderId);

            // when & then
            mockMvc.perform(delete("/api/v2/orders/{id}", orderId))
                    .andExpect(status().isNotFound());
        }
    }
}
