package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer stepNumber; // 1: Confirmed, 2: Arranging Blooms, 3: Out for Delivery, 4: Delivered

    @Column(nullable = false, length = 100)
    private String statusTitle;

    @Column(columnDefinition = "TEXT")
    private String statusDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    private LocalDateTime completedAt;

    @Column(length = 100)
    private String driverName;

    @Column(length = 20)
    private String driverPhone;

    private LocalDateTime estimatedDeliveryTime;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
