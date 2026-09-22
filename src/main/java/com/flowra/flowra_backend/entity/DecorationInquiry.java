package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "decoration_inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecorationInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String clientName;

    @Column(nullable = false, length = 255)
    private String clientEmail;

    @Column(nullable = false, length = 20)
    private String clientPhone;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String eventType = "wedding";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String venueScale = "medium";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String themeColor = "blush";

    private LocalDate eventDate;

    private BigDecimal estimatedBudget;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "Pending";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
