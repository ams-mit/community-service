package kln.ams.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visitor_history", indexes = {
        @Index(name = "idx_vh_visitor_id", columnList = "visitor_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorHistory {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "visitor_id", length = 36, nullable = false)
    private String visitorId;

    @Column(name = "event", nullable = false, length = 50)
    private String event;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private VisitorStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30)
    private VisitorStatus newStatus;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.changedAt == null) {
            this.changedAt = Instant.now();
        }
    }
}
