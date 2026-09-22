package com.flowra.flowra_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RazorpayDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemRequest {
        private String id;
        private Integer quantity;
        private BigDecimal price;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOrderRequest {
        private BigDecimal amount; // In rupees, e.g. 150.00
        private String currency; // Default "INR"
        private String receipt; // e.g. "FLW-123456"
        private Long orderId; // Optional link to existing DB order
        private String promoCode;
        private List<CartItemRequest> items;
        private Map<String, String> notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOrderResponse {
        private String razorpayOrderId;
        private BigDecimal amount; // In rupees
        private Long amountInPaise; // In paise for Razorpay JS modal
        private String currency;
        private String keyId;
        private String appOrderId;
        private String companyName;
        private String status;
        private boolean isTestMode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyPaymentRequest {
        @JsonProperty("razorpay_order_id")
        @JsonAlias({"razorpayOrderId", "razorpay_order_id"})
        private String razorpayOrderId;

        @JsonProperty("razorpay_payment_id")
        @JsonAlias({"razorpayPaymentId", "razorpay_payment_id"})
        private String razorpayPaymentId;

        @JsonProperty("razorpay_signature")
        @JsonAlias({"razorpaySignature", "razorpay_signature"})
        private String razorpaySignature;

        private String appOrderId;
        private Long orderId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyPaymentResponse {
        private boolean verified;
        private String message;
        private String razorpayPaymentId;
        private String razorpayOrderId;
        private String orderStatus;
        private PaymentDTO.Response payment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KeyResponse {
        private String keyId;
        private String currency;
        private String companyName;
    }
}
