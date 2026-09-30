package kln.ams.community.dto;

import kln.ams.community.entity.Notification;
import kln.ams.community.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String recipientUserId;
    private String type;
    private String title;
    private String message;
    private String priority;
    private NotificationStatus status;
    private String deliveryStatus;
    private String sourceService;
    private String sourceEntityType;
    private String sourceEntityId;
    private Instant readAt;
    private Instant createdAt;
    private Instant expiresAt;

    public static NotificationResponse fromEntity(Notification n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipientUserId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .priority(n.getPriority())
                .status(n.getStatus())
                .deliveryStatus(n.getDeliveryStatus())
                .sourceService(n.getSourceService())
                .sourceEntityType(n.getSourceEntityType())
                .sourceEntityId(n.getSourceEntityId())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .expiresAt(n.getExpiresAt())
                .build();
    }
}
