package kln.ams.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipient_user_id"),
        @Index(name = "idx_notif_status", columnList = "status"),
        @Index(name = "idx_notif_type", columnList = "type"),
        @Index(name = "idx_notif_created_at", columnList = "created_at"),
        @Index(name = "idx_notif_dedup", columnList = "source_service, source_entity_type, source_entity_id, type, recipient_user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "recipient_user_id", length = 36, nullable = false)
    private String recipientUserId;

    @Column(name = "type", nullable = false, length = 60)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "priority", length = 30)
    @Builder.Default
    private String priority = "NORMAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "delivery_status", length = 30)
    @Builder.Default
    private String deliveryStatus = "DELIVERED";

    @Column(name = "source_service", length = 60)
    private String sourceService;

    @Column(name = "source_entity_type", length = 60)
    private String sourceEntityType;

    @Column(name = "source_entity_id", length = 36)
    private String sourceEntityId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = NotificationStatus.UNREAD;
        }
        if (this.deliveryStatus == null) {
            this.deliveryStatus = "DELIVERED";
        }
        if (this.priority == null) {
            this.priority = "NORMAL";
        }
    }
}
