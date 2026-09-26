package lk.ac.kln.apartment.community_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id")
    private String recipientId; // User ID or Resident ID (null or "ALL" for broadcast)

    @Column(name = "recipient_role")
    private String recipientRole; // "RESIDENT", "TENANT", "OWNER", "APARTMENT_MANAGER", or "ALL"

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type; // maintenance, security, facility, announcement, billing, general

    private String category; // MAINTENANCE, SECURITY, FACILITY, BULLETIN, FINANCE, GENERAL

    private String priority; // urgent, high, normal, low

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "affected_area")
    private String affectedArea;

    @Column(name = "action_route")
    private String actionRoute;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.priority == null) {
            this.priority = "normal";
        }
        if (this.category == null) {
            this.category = "GENERAL";
        }
        if (this.type == null) {
            this.type = "general";
        }
    }
}
