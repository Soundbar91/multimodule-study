package com.soundbar91.order.infrastructure.repository;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.vo.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA Repository
 */
public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByShopId(Long shopId);

    List<Order> findByStatus(OrderStatus status);
}
