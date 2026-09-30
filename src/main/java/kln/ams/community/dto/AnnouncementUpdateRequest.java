package kln.ams.community.dto;

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
public class AnnouncementUpdateRequest {

    private String title;
    private String content;
    private String category;
    private String priority;
    private AudienceType audienceType;
    private List<String> roleCodes;
    private List<String> unitIds;
    private Instant scheduledAt;
    private Instant expiresAt;
}
