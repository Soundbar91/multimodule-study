package com.soundbar91.order.service;

import com.soundbar91.common.exception.NotFoundException;
import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.event.OrderCancelledEvent;
import com.soundbar91.order.domain.event.OrderCreatedEvent;
import com.soundbar91.order.domain.repository.OrderRepository;
import com.soundbar91.order.domain.vo.OrderStatus;
import com.soundbar91.shop.service.ShopService;
import com.soundbar91.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 서비스
 * 도메인 간 직접 호출과 이벤트 기반 통신을 모두 사용
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;  // 도메인 간 직접 의존
    private final ShopService shopService;  // 도메인 간 직접 의존
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        UserService userService,
                        ShopService shopService,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.shopService = shopService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 주문 생성
     * - 도메인 간 직접 호출: 사용자/상점 존재 여부 검증
     * - 이벤트 발행: 주문 생성 후 OrderCreatedEvent 발행
     */
    @Transactional
    public Order createOrder(Long userId, Long shopId, String productName, Integer quantity, BigDecimal totalAmount, String deliveryAddress) {
        // 도메인 간 직접 호출로 사용자 존재 여부 검증
        if (!userService.existsById(userId)) {
            throw new NotFoundException("주문자를 찾을 수 없습니다. User ID: " + userId);
        }

        // 도메인 간 직접 호출로 상점 존재 여부 검증
        if (!shopService.existsById(shopId)) {
            throw new NotFoundException("상점을 찾을 수 없습니다. Shop ID: " + shopId);
        }

        Order order = new Order(userId, shopId, productName, quantity, totalAmount, deliveryAddress);
        Order savedOrder = orderRepository.save(order);

        // 도메인 이벤트 발행 (느슨한 결합)
        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getShopId(),
                savedOrder.getProductName(),
                savedOrder.getTotalAmount()
        ));

        return savedOrder;
    }

    /**
     * 주문 조회
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 모든 주문 조회
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * 사용자별 주문 조회
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 상점별 주문 조회
     */
    public List<Order> getOrdersByShopId(Long shopId) {
        return orderRepository.findByShopId(shopId);
    }

    /**
     * 상태별 주문 조회
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * 주문 확정
     */
    @Transactional
    public Order confirmOrder(Long id) {
        Order order = getOrderById(id);
        order.confirm();
        return order;
    }

    /**
     * 주문 배송 시작
     */
    @Transactional
    public Order shipOrder(Long id) {
        Order order = getOrderById(id);
        order.ship();
        return order;
    }

    /**
     * 주문 배송 완료
     */
    @Transactional
    public Order deliverOrder(Long id) {
        Order order = getOrderById(id);
        order.deliver();
        return order;
    }

    /**
     * 주문 취소
     * 취소 후 OrderCancelledEvent 발행
     */
    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.cancel();

        // 도메인 이벤트 발행
        eventPublisher.publishEvent(new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                order.getShopId()
        ));

        return order;
    }

    /**
     * 주문 삭제
     */
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}
