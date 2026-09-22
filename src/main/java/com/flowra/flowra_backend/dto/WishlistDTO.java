package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotNull(message = "Product ID is required")
        private Long productId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private Long productId;
        private String productName;
        private String productSlug;
        private BigDecimal price;
        private String imageUrl;
        private String category;
        private LocalDateTime createdAt;
    }
}
