package com.flowra.flowra_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class SupplierDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Supplier name is mandatory")
        private String name;
        private String contactPerson;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String contactPerson;
        private String phone;
        private String email;
        private String address;
        private LocalDateTime createdAt;
    }
}
