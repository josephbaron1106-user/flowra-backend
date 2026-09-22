package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flower_id", nullable = false, unique = true)
    private Flower flower;

    @Column(name = "total_inflow", nullable = false)
    @Builder.Default
    private Integer totalInflow = 0;

    @Column(name = "total_outflow", nullable = false)
    @Builder.Default
    private Integer totalOutflow = 0;

    @Column(name = "current_stock", nullable = false)
    @Builder.Default
    private Integer currentStock = 0;

    @Column(name = "min_threshold", nullable = false)
    @Builder.Default
    private Integer minThreshold = 10;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.totalInflow == null) this.totalInflow = 0;
        if (this.totalOutflow == null) this.totalOutflow = 0;
        if (this.currentStock == null) this.currentStock = this.totalInflow - this.totalOutflow;
        if (this.minThreshold == null) this.minThreshold = 10;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
