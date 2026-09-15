package lk.ac.kln.apartment.community_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequest {

    private String title;
    private String content;
    private String targetRole;

    // Temporary — should come from the verified JWT once security is added
    private String publishedBy;
}