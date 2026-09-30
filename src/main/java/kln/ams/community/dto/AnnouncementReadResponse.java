package kln.ams.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementReadResponse {
    private String announcementId;
    private String userId;
    private Instant readAt;
}
