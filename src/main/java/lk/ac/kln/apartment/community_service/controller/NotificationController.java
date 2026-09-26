package lk.ac.kln.apartment.community_service.controller;

import jakarta.validation.Valid;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import lk.ac.kln.apartment.community_service.dto.NotificationResponse;
import lk.ac.kln.apartment.community_service.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * COMM-008: Create workflow notification
     * Method: POST
     * Endpoint: /api/v1/notifications
     * API Type: INTERNAL
     * Authentication: Gateway Service JWT
     * Required Role: Authorized services
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(@Valid @RequestBody NotificationRequest request) {
        return notificationService.createNotification(request);
    }

    /**
     * COMM-009: List current user's notifications
     * Method: GET
     * Endpoint: /api/v1/notifications
     * API Type: USER
     * Authentication: Gateway User JWT
     * Required Role: Authenticated roles
     */
    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String headerUserRole) {

        String effectiveRecipientId = (recipientId != null && !recipientId.isBlank()) ? recipientId : headerUserId;
        String effectiveRole = (role != null && !role.isBlank()) ? role : headerUserRole;

        return notificationService.getNotifications(effectiveRecipientId, effectiveRole, unreadOnly);
    }

    /**
     * Get single notification by ID
     */
    @GetMapping("/{notificationId}")
    public NotificationResponse getNotificationById(@PathVariable Long notificationId) {
        return notificationService.getNotificationById(notificationId);
    }

    /**
     * COMM-010: Mark notification as read
     * Method: PATCH
     * Endpoint: /api/v1/notifications/{notificationId}/read
     * API Type: USER
     * Authentication: Gateway User JWT
     * Required Role: Authenticated user
     */
    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(@PathVariable Long notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read for current user
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestParam(required = false) String recipientId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String effectiveRecipientId = (recipientId != null && !recipientId.isBlank()) ? recipientId : headerUserId;
        int count = notificationService.markAllAsRead(effectiveRecipientId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read",
                "updatedCount", count
        ));
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
