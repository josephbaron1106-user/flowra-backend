package com.flowra.flowra_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityResponse {
        private Long id;
        private String type;
        private String title;
        private String detail;
        private BigDecimal amount;
        private String icon;
        private String badgeClass;
        private LocalDateTime time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardResponse {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Long pendingOrders;
        private Long deliveredOrders;
        private Long totalCustomers;
        private Long totalProducts;
        private Long lowStockCount;
        private List<ActivityResponse> recentActivities;
    }
}
