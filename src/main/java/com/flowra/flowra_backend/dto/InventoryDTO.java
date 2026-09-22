package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class InventoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stock;

        private Integer reservedStock;
        private Integer minThreshold;
        private Integer delta; // For increment / decrement operations
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer currentStock;
        private Integer reservedStock;
        private Integer minThreshold;
        private Boolean isLowStock;
        private LocalDateTime lastRestockedAt;
        private LocalDateTime updatedAt;
    }
}
