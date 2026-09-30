package kln.ams.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "announcement_reads", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ann_read_user", columnNames = {"announcement_id", "user_id"})
}, indexes = {
        @Index(name = "idx_ar_announcement_id", columnList = "announcement_id"),
        @Index(name = "idx_ar_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "announcement_id", length = 36, nullable = false)
    private String announcementId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.readAt == null) {
            this.readAt = Instant.now();
        }
    }
}
