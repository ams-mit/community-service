package kln.ams.community.dto;

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
public class NotificationStatusResponse {
    private String notificationId;
    private NotificationStatus status;
    private String deliveryStatus;
    private Instant readAt;
}
