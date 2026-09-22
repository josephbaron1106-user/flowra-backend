package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long categoryId;

        @NotBlank(message = "Product name is required")
        private String name;

        private String slug;
        private String sku;
        private String flowerType;
        private String color;
        private String size;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
        private BigDecimal price;

        private BigDecimal originalPrice;
        private Integer discountPercent;
        private String description;
        private String careInstructions;
        private Integer popularity;
        private String status;
        private Boolean isFeatured;
        private List<String> imageUrls;
        private Integer initialStock;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String name;
        private String slug;
        private String sku;
        private String flowerType;
        private String color;
        private String size;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer discountPercent;
        private String description;
        private String careInstructions;
        private Integer popularity;
        private String status;
        private Boolean isFeatured;
        private Integer currentStock;
        private List<String> images;
        private String primaryImage;
        private LocalDateTime createdAt;
    }
}
