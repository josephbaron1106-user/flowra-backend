package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.DecorationInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecorationInquiryRepository extends JpaRepository<DecorationInquiry, Long> {
    List<DecorationInquiry> findByStatus(String status);
}
