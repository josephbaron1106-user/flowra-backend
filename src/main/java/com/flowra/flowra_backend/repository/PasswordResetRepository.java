package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findTopByEmailOrderByCreatedAtDesc(String email);
}
