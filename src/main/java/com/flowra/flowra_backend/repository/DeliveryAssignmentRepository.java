package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByDeliveryCode(String deliveryCode);
    List<DeliveryAssignment> findByStatus(String status);
}
