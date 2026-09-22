package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByFlowerId(Long flowerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.flower.id = :flowerId")
    Optional<Stock> findByFlowerIdWithLock(@Param("flowerId") Long flowerId);

    @Query("SELECT s FROM Stock s WHERE s.currentStock <= s.minThreshold")
    List<Stock> findLowStock();

    @Query("SELECT s FROM Stock s WHERE s.currentStock = 0")
    List<Stock> findOutOfStock();

    List<Stock> findAllByOrderByCurrentStockAsc();
}
