package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByPlacedAtDesc(Long userId);
    List<Order> findAllByOrderByPlacedAtDesc();
    List<Order> findByStatusOrderByPlacedAtDesc(String status);
    List<Order> findByStatusInOrderByPlacedAtDesc(List<String> statuses);
    Optional<Order> findByOrderNumber(String orderNumber);
}
