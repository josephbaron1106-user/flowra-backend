package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.PasswordReset;
import com.flowra.flowra_backend.repository.PasswordResetRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetRepository passwordResetRepository;

    @Data
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestOtp(@RequestBody ForgotPasswordRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        String cleanEmail = req.getEmail().trim().toLowerCase();
        String generatedOtp = String.format("%06d", new Random().nextInt(999999));

        PasswordReset reset = PasswordReset.builder()
                .email(cleanEmail)
                .otpCode(generatedOtp)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetRepository.save(reset);
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully to " + cleanEmail,
                "email", cleanEmail,
                "demoOtp", generatedOtp // Convenient for testing
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        if (req.getEmail() == null || req.getOtp() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }
        String cleanEmail = req.getEmail().trim().toLowerCase();
        PasswordReset record = passwordResetRepository.findTopByEmailOrderByCreatedAtDesc(cleanEmail)
                .orElse(null);

        if (record == null || !record.getOtpCode().equals(req.getOtp()) || Boolean.TRUE.equals(record.getIsUsed())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP code"));
        }

        record.setIsUsed(true);
        passwordResetRepository.save(record);
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully!"));
    }
}
