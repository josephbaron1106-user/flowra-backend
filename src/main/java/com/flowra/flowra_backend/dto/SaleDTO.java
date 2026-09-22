package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String receiptNumber;

        private Long flowerId;
        private String flowerName;

        @NotNull(message = "Quantity is mandatory")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Selling price is mandatory")
        private BigDecimal unitPrice;

        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String paymentMode;
        private String customerName;
        private String customerPhone;
        private String attendantName;
        private String saleType; // "POS" or "ONLINE"
        private Long orderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String receiptNumber;
        private Long flowerId;
        private String flowerName;
        private String categoryName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String paymentMode;
        private String customerName;
        private String customerPhone;
        private String attendantName;
        private String saleType;
        private Long orderId;
        private Integer remainingStock;
        private LocalDateTime saleTime;
    }
}
