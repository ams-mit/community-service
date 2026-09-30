package kln.ams.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalNotificationRequest {

    @NotBlank(message = "recipientUserId is required")
    private String recipientUserId;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "message is required")
    private String message;

    @Builder.Default
    private String priority = "NORMAL";

    private String sourceService;

    private String sourceEntityType;

    private String sourceEntityId;
}
