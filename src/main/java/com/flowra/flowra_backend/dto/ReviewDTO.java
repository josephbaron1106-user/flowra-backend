package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class ReviewDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long userId;

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotBlank(message = "Author name is required")
        private String authorName;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;

        @NotBlank(message = "Comment is required")
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminReplyRequest {
        @NotBlank(message = "Reply text is required")
        private String adminReply;
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
        private String authorName;
        private Integer rating;
        private String comment;
        private String status;
        private String adminReply;
        private LocalDateTime adminRepliedAt;
        private LocalDateTime createdAt;
    }
}
