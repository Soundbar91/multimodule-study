package com.soundbar91.api.order.controller;

import com.soundbar91.domain.order.entity.Order;
import com.soundbar91.domain.order.service.OrderService;
import com.soundbar91.api.order.dto.request.CreateOrderRequest;
import com.soundbar91.api.order.dto.response.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 API Controller
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 주문 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.getUserId(),
                request.getShopId(),
                request.getProductName(),
                request.getQuantity(),
                request.getTotalAmount(),
                request.getDeliveryAddress()
        );
        return OrderResponse.from(order);
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return OrderResponse.from(order);
    }

    /**
     * 모든 주문 조회
     */
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자별 주문 조회
     */
    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상점별 주문 조회
     */
    @GetMapping("/shop/{shopId}")
    public List<OrderResponse> getOrdersByShopId(@PathVariable Long shopId) {
        return orderService.getOrdersByShopId(shopId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 확정
     */
    @PostMapping("/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return OrderResponse.from(order);
    }

    /**
     * 주문 배송 시작
     */
    @PostMapping("/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        Order order = orderService.shipOrder(id);
        return OrderResponse.from(order);
    }

    /**
     * 주문 배송 완료
     */
    @PostMapping("/{id}/deliver")
    public OrderResponse deliverOrder(@PathVariable Long id) {
        Order order = orderService.deliverOrder(id);
        return OrderResponse.from(order);
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return OrderResponse.from(order);
    }

    /**
     * 주문 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
