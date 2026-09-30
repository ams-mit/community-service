package kln.ams.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AnnouncementCreateRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "content is required")
    private String content;

    private String category;

    private String priority;

    @NotNull(message = "audienceType is required")
    private AudienceType audienceType;

    private List<String> roleCodes;

    private List<String> unitIds;

    private Instant scheduledAt;

    private Instant expiresAt;
}
