package com.flowra.flowra_backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long flowerId;
        private String flowerName;
        private String flowerSku;
        private String flowerSlug;
        private String categoryName;
        private String imageUrl;
        private BigDecimal retailPrice;
        private Integer totalInflow;
        private Integer totalOutflow;
        private Integer currentStock;
        private Integer minThreshold;
        private String status; // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
        private String statusLabel; // "In Stock", "Low Stock Alert", "Out of Stock"
        private LocalDateTime lastRestockedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateThresholdRequest {
        private Integer minThreshold;
    }
}
