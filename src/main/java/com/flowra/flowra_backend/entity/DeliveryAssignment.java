package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String deliveryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false, length = 50)
    private String orderNumber;

    @Column(nullable = false, length = 150)
    private String customerName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(nullable = false, length = 150)
    private String deliveryPartner;

    @Column(nullable = false, length = 150)
    private String driverName;

    @Column(length = 30)
    private String driverPhone;

    @Column(nullable = false, length = 100)
    private String deliverySlot;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "Assigned";

    @Column(length = 100)
    @Builder.Default
    private String temperatureLog = "15°C (Climate-Controlled)";

    @Column(updatable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}
