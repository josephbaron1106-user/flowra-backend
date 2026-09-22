package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String purchaseCode;

        private Long flowerId;
        private String flowerName; // In case user selected by name

        private Long supplierId;
        private String supplierName; // In case user selected by name

        @NotNull(message = "Quantity is mandatory")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit cost price is mandatory")
        private BigDecimal unitCost;

        private BigDecimal retailPrice;
        private String grade;
        private String notes;
        private String purchaseDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String purchaseCode;
        private Long flowerId;
        private String flowerName;
        private String flowerSku;
        private String categoryName;
        private Long supplierId;
        private String supplierName;
        private Integer quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
        private BigDecimal retailPrice;
        private String grade;
        private String notes;
        private LocalDate purchaseDate;
        private Integer resultingStock;
        private LocalDateTime createdAt;
    }
}
