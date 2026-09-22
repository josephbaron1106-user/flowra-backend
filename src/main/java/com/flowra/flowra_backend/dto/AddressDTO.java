package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class AddressDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        @NotBlank(message = "Recipient phone is required")
        private String recipientPhone;

        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotBlank(message = "City is required")
        private String city;

        private String state;

        @NotBlank(message = "Pincode is required")
        private String pincode;

        private String country;
        private String addressType; // 'Home', 'Work', 'Other'
        private Boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String recipientName;
        private String recipientPhone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private String addressType;
        private Boolean isDefault;
        private LocalDateTime createdAt;
    }
}
