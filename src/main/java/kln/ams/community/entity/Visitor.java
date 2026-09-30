package kln.ams.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "visitors", indexes = {
        @Index(name = "idx_vis_resident", columnList = "resident_user_id"),
        @Index(name = "idx_vis_unit", columnList = "unit_id"),
        @Index(name = "idx_vis_status", columnList = "status"),
        @Index(name = "idx_vis_date", columnList = "visit_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "resident_user_id", length = 36, nullable = false)
    private String residentUserId;

    @Column(name = "unit_id", length = 36, nullable = false)
    private String unitId;

    @Column(name = "visitor_name", nullable = false)
    private String visitorName;

    @Column(name = "visitor_contact")
    private String visitorContact;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "expected_arrival")
    private String expectedArrival;

    @Column(name = "expected_departure")
    private String expectedDeparture;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private VisitorStatus status = VisitorStatus.PENDING;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_by_user_id", length = 36)
    private String createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = VisitorStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
