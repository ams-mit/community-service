package lk.ac.kln.apartment.community_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String recipientId;
    private String recipientRole;
    private String title;
    private String summary;
    private String message;
    private String type;
    private String category;
    private String priority;
    private String issuedBy;
    private String affectedArea;
    private String actionRoute;
    private String actionLabel;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
