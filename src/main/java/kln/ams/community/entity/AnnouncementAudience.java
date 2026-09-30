package kln.ams.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "announcement_audiences", indexes = {
        @Index(name = "idx_aa_announcement_id", columnList = "announcement_id"),
        @Index(name = "idx_aa_role_code", columnList = "role_code"),
        @Index(name = "idx_aa_unit_id", columnList = "unit_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementAudience {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "announcement_id", length = 36, nullable = false)
    private String announcementId;

    @Column(name = "audience_type", length = 30, nullable = false)
    private String audienceType;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "unit_id", length = 36)
    private String unitId;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
