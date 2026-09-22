package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateItemRequest {
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private String productImage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long userId; // Optional for guest checkout
        private String orderNumber; // Optional custom order number from client

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        @NotBlank(message = "Recipient phone is required")
        private String recipientPhone;

        @NotBlank(message = "Delivery address is required")
        private String deliveryAddress;

        private LocalDate deliveryDate;
        private String deliverySlot;
        private String giftMessage;
        private String occasion;
        private String promoCode;
        private BigDecimal discountAmount;
        private BigDecimal deliveryFee;

        // Payment info if supplied during checkout
        private String paymentMethod; // 'razorpay', 'cod'
        private String paymentId;
        private String razorpayOrderId;

        @NotEmpty(message = "Order must contain at least one item")
        private List<CreateItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {
        @NotBlank(message = "Status is required")
        private String status; // 'PLACED', 'CONFIRMED', 'PROCESSING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'
        private String cancellationReason;
        private String cancelledBy; // 'Customer' or 'Admin'
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String productImage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String orderNumber;
        private Long userId;
        private String status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal deliveryFee;
        private BigDecimal totalAmount;
        private String promoCode;
        private LocalDate deliveryDate;
        private String deliverySlot;
        private String recipientName;
        private String recipientPhone;
        private String deliveryAddress;
        private String giftMessage;
        private String occasion;
        private String cancellationReason;
        private String cancelledBy;
        private List<ItemResponse> items;
        private List<OrderTrackingDTO.Response> tracking;
        private List<PaymentDTO.Response> payments;
        private LocalDateTime placedAt;
        private LocalDateTime updatedAt;
    }
}
