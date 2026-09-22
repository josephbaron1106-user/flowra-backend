package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.Coupon;
import com.flowra.flowra_backend.repository.CouponRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;

    @Data
    public static class CouponRequest {
        private String code;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private String expiryDate;
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody CouponRequest req) {
        String cleanCode = req.getCode().trim().toUpperCase();
        if (couponRepository.existsByCode(cleanCode)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code already exists: " + cleanCode));
        }

        LocalDate exp = null;
        if (req.getExpiryDate() != null && !req.getExpiryDate().isBlank()) {
            try { exp = LocalDate.parse(req.getExpiryDate()); } catch (Exception ignored) {}
        }

        Coupon coupon = Coupon.builder()
                .code(cleanCode)
                .discountType(req.getDiscountType() != null ? req.getDiscountType() : "Percentage")
                .discountValue(req.getDiscountValue() != null ? req.getDiscountValue() : BigDecimal.TEN)
                .minOrderAmount(req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(req.getMaxDiscountAmount())
                .usageLimit(req.getUsageLimit() != null ? req.getUsageLimit() : 100)
                .usageCount(0)
                .status("Active")
                .expiryDate(exp)
                .build();

        return new ResponseEntity<>(couponRepository.save(coupon), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleCoupon(@PathVariable Long id) {
        Coupon c = couponRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        c.setStatus("Active".equals(c.getStatus()) ? "Disabled" : "Active");
        return ResponseEntity.ok(couponRepository.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        if (!couponRepository.existsById(id)) return ResponseEntity.notFound().build();
        couponRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
