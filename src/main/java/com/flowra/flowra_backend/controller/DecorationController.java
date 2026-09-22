package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.DecorationInquiry;
import com.flowra.flowra_backend.repository.DecorationInquiryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/decorations")
@RequiredArgsConstructor
public class DecorationController {

    private final DecorationInquiryRepository inquiryRepository;

    @Data
    public static class InquiryRequest {
        private String clientName;
        private String clientEmail;
        private String clientPhone;
        private String eventType;
        private String venueScale;
        private String themeColor;
        private String eventDate;
        private BigDecimal estimatedBudget;
        private String notes;
    }

    @PostMapping
    public ResponseEntity<DecorationInquiry> createInquiry(@RequestBody InquiryRequest req) {
        LocalDate parsedDate = null;
        if (req.getEventDate() != null && !req.getEventDate().isBlank()) {
            try {
                parsedDate = LocalDate.parse(req.getEventDate());
            } catch (Exception ignored) {}
        }

        DecorationInquiry inquiry = DecorationInquiry.builder()
                .clientName(req.getClientName())
                .clientEmail(req.getClientEmail())
                .clientPhone(req.getClientPhone())
                .eventType(req.getEventType() != null ? req.getEventType() : "wedding")
                .venueScale(req.getVenueScale() != null ? req.getVenueScale() : "medium")
                .themeColor(req.getThemeColor() != null ? req.getThemeColor() : "blush")
                .eventDate(parsedDate)
                .estimatedBudget(req.getEstimatedBudget())
                .notes(req.getNotes())
                .status("Pending")
                .build();

        return new ResponseEntity<>(inquiryRepository.save(inquiry), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DecorationInquiry>> getAllInquiries() {
        return ResponseEntity.ok(inquiryRepository.findAll());
    }
}
