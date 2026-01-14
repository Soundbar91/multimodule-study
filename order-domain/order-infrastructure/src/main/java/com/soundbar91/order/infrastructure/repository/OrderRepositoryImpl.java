package com.soundbar91.order.infrastructure.repository;

import com.soundbar91.order.domain.entity.Order;
import com.soundbar91.order.domain.repository.OrderRepository;
import com.soundbar91.order.domain.vo.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository 구현체
 * JPA를 사용한 영속성 계층 구현
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Order> findByShopId(Long shopId) {
        return orderJpaRepository.findByShopId(shopId);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderJpaRepository.findByStatus(status);
    }

    @Override
    public void delete(Order order) {
        orderJpaRepository.delete(order);
    }

    @Override
    public void deleteById(Long id) {
        orderJpaRepository.deleteById(id);
    }
}
