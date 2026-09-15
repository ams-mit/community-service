package lk.ac.kln.apartment.community_service.dto;

import lk.ac.kln.apartment.community_service.entity.AnnouncementStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    private Long id;
    private String title;
    private String content;
    private String targetRole;
    private String publishedBy;
    private AnnouncementStatus status;
    private LocalDateTime createdAt;
}