package lk.ac.kln.apartment.community_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String recipientId;

    private String recipientRole;

    @NotBlank(message = "Title is required")
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
}
