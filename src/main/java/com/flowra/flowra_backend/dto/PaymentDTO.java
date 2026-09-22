package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Order ID is required")
        private Long orderId;

        @NotBlank(message = "Payment method is required")
        private String paymentMethod; // 'razorpay', 'cod', 'card', 'upi'

        private String paymentStatus; // 'Pending', 'Authorized', 'Paid', 'Failed', 'Refunded'
        private BigDecimal amount;
        private String currency;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long orderId;
        private String paymentMethod;
        private String paymentStatus;
        private BigDecimal amount;
        private String currency;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }
}
