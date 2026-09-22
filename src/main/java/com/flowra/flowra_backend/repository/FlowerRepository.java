package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowerRepository extends JpaRepository<Flower, Long> {
    Optional<Flower> findByNameIgnoreCase(String name);
    Optional<Flower> findBySku(String sku);
    Optional<Flower> findBySlug(String slug);
    List<Flower> findByCategoryId(Long categoryId);
    List<Flower> findByStatus(String status);
}
