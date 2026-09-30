package kln.ams.community.dto;

import kln.ams.community.entity.Announcement;
import kln.ams.community.entity.AnnouncementStatus;
import kln.ams.community.entity.AudienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    private String id;
    private String title;
    private String content;
    private String category;
    private String priority;
    private AnnouncementStatus status;
    private AudienceType audienceType;
    private List<String> roleCodes;
    private List<String> unitIds;
    private Instant scheduledAt;
    private Instant publishedAt;
    private Instant expiresAt;
    private String cancelReason;
    private Boolean isRead;
    private String createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static AnnouncementResponse fromEntity(Announcement a, List<String> roles, List<String> units, Boolean isRead) {
        if (a == null) return null;
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .priority(a.getPriority())
                .status(a.getStatus())
                .audienceType(a.getAudienceType())
                .roleCodes(roles)
                .unitIds(units)
                .scheduledAt(a.getScheduledAt())
                .publishedAt(a.getPublishedAt())
                .expiresAt(a.getExpiresAt())
                .cancelReason(a.getCancelReason())
                .isRead(isRead)
                .createdByUserId(a.getCreatedByUserId())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
