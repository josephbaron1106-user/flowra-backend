package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FlowerDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Flower name is mandatory")
        private String name;

        private Long categoryId;
        private String categoryName;

        private String sku;
        private BigDecimal price;
        private String imageUrl;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String slug;
        private String sku;
        private BigDecimal price;
        private String imageUrl;
        private String status;
        private Long categoryId;
        private String categoryName;
        private Integer availableStock;
        private LocalDateTime createdAt;
    }
}
