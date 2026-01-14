package com.soundbar91.order.api.controller;

import com.soundbar91.order.api.dto.request.CreateOrderRequest;
import com.soundbar91.order.api.dto.response.OrderResponse;
import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.userId(),
                request.shopId(),
                request.productName(),
                request.quantity(),
                request.totalAmount(),
                request.deliveryAddress()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 모든 주문 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders().stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    /**
     * 사용자별 주문 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    /**
     * 상점별 주문 조회
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByShopId(@PathVariable Long shopId) {
        List<OrderResponse> orders = orderService.getOrdersByShopId(shopId).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    /**
     * 상태별 주문 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    /**
     * 주문 확정
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 주문 배송 시작
     */
    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable Long id) {
        Order order = orderService.shipOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 주문 배송 완료
     */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id) {
        Order order = orderService.deliverOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 주문 취소
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    /**
     * 주문 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
