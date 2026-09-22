package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class OrderTrackingDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Order ID is required")
        private Long orderId;

        private Integer stepNumber; // 1: Confirmed, 2: Arranging Blooms, 3: Out for Delivery, 4: Delivered
        private String statusTitle;
        private String statusDescription;
        private Boolean isCompleted;
        private String driverName;
        private String driverPhone;
        private LocalDateTime estimatedDeliveryTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long orderId;
        private Integer stepNumber;
        private String statusTitle;
        private String statusDescription;
        private Boolean isCompleted;
        private LocalDateTime completedAt;
        private String driverName;
        private String driverPhone;
        private LocalDateTime estimatedDeliveryTime;
        private LocalDateTime createdAt;
    }
}
