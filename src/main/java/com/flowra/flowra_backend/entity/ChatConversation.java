package com.flowra.flowra_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(nullable = false, length = 150)
    private String customerName;

    @Column(length = 50)
    @Builder.Default
    private String customerAvatar = "👤";

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOnline = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer unreadCount = 0;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

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
