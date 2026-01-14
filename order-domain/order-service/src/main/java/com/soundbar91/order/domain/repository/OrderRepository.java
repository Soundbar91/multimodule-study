package com.soundbar91.order.domain.repository;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * 주문 레포지토리 인터페이스
 * 도메인 계층에서 정의하고, infrastructure 계층에서 구현
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findByUserId(Long userId);

    List<Order> findByShopId(Long shopId);

    List<Order> findByStatus(OrderStatus status);

    void delete(Order order);

    void deleteById(Long id);
}
