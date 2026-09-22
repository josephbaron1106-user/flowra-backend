package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_mode", nullable = false, length = 50)
    @Builder.Default
    private String paymentMode = "Cash";

    @Column(name = "customer_name", nullable = false, length = 150)
    @Builder.Default
    private String customerName = "Walk-in Guest";

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "attendant_name", nullable = false, length = 150)
    @Builder.Default
    private String attendantName = "Counter Staff";

    @Column(name = "sale_type", nullable = false, length = 20)
    @Builder.Default
    private String saleType = "POS"; // "POS" or "ONLINE"

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "sale_time")
    private LocalDateTime saleTime;

    @PrePersist
    protected void onCreate() {
        if (this.saleTime == null) this.saleTime = LocalDateTime.now();
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
        if (this.totalAmount == null && this.unitPrice != null && this.quantity != null) {
            this.totalAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)).subtract(this.discountAmount);
        }
    }
}
