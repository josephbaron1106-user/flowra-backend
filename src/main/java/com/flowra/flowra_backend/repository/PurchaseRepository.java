package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByPurchaseCode(String purchaseCode);
    List<Purchase> findByFlowerIdOrderByPurchaseDateDesc(Long flowerId);
    List<Purchase> findBySupplierId(Long supplierId);
    List<Purchase> findAllByOrderByPurchaseDateDesc();
}
