package kln.ams.community.dto;

import kln.ams.community.entity.AudienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudiencePreviewResponse {
    private String announcementId;
    private AudienceType audienceType;
    private long eligibleRecipientCount;
    private List<String> roleCodes;
    private List<String> unitIds;
}
