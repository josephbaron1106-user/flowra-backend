package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class CategoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Category name is required")
        private String name;

        private String slug;
        private String description;
        private String imageUrl;
        private String status;
        private Integer displayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
        private String status;
        private Integer displayOrder;
        private Integer productCount;
        private LocalDateTime createdAt;
    }
}
