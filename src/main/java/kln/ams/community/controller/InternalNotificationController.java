package kln.ams.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kln.ams.community.dto.*;
import kln.ams.community.security.SecurityUtils;
import kln.ams.community.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/notifications")
@Tag(name = "Internal Notifications", description = "Service-to-service internal notification ingestion endpoints")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * COMM-INT-001: Internal Notification Creation
     */
    @PostMapping
    @Operation(summary = "COMM-INT-001: Create Notification (Internal)", description = "Service-to-service endpoint to request an in-application notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> createInternalNotification(
            @Valid @RequestBody InternalNotificationRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationResponse response = notificationService.createInternalNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Notification created successfully", response, requestId));
    }

    /**
     * COMM-INT-002: Internal Notification Status
     */
    @GetMapping("/{notificationId}/status")
    @Operation(summary = "COMM-INT-002: Get Notification Status (Internal)", description = "Check delivery/read state of a notification internally")
    public ResponseEntity<ApiResponse<NotificationStatusResponse>> getInternalNotificationStatus(
            @PathVariable String notificationId) {

        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationStatusResponse response = notificationService.getInternalNotificationStatus(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification status retrieved", response, requestId));
    }

    /**
     * COMM-INT-003: Internal Resident Notification Request
     */
    @PostMapping("/resident")
    @Operation(summary = "COMM-INT-003: Resident Notification Request (Internal)", description = "Service-to-service endpoint targeting a resident domain event")
    public ResponseEntity<ApiResponse<NotificationResponse>> createInternalResidentNotification(
            @Valid @RequestBody InternalResidentNotificationRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationResponse response = notificationService.createInternalResidentNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Resident notification created successfully", response, requestId));
    }
}
