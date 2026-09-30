package kln.ams.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kln.ams.community.dto.*;
import kln.ams.community.entity.NotificationStatus;
import kln.ams.community.security.SecurityUtils;
import kln.ams.community.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Center", description = "Public user notification endpoints for Project A Community Service")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * COMM-NOT-001: List My Notifications
     */
    @GetMapping
    @Operation(summary = "COMM-NOT-001: List My Notifications", description = "List notifications scoped strictly to the authenticated user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listMyNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String requestId = SecurityUtils.getCurrentRequestId();
        Page<NotificationResponse> resultPage = notificationService.listMyNotifications(
                status, type, priority, unreadOnly, dateFrom, dateTo, page, size
        );

        PaginationDto pagination = PaginationDto.fromPage(resultPage);
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved successfully", resultPage.getContent(), pagination, requestId));
    }

    /**
     * COMM-NOT-002: Get Notification
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "COMM-NOT-002: Get Notification", description = "Get single notification by ID scoped to current recipient")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable String notificationId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationResponse response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification retrieved successfully", response, requestId));
    }

    /**
     * COMM-NOT-003: Mark Notification Read
     */
    @PostMapping("/{notificationId}/read")
    @Operation(summary = "COMM-NOT-003: Mark Notification Read", description = "Mark notification as read with server timestamp")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable String notificationId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", response, requestId));
    }

    /**
     * COMM-NOT-004: Mark All Notifications Read
     */
    @PostMapping("/read-all")
    @Operation(summary = "COMM-NOT-004: Mark All Notifications Read", description = "Mark all unread notifications of authenticated user as read")
    public ResponseEntity<ApiResponse<NotificationReadAllResponse>> markAllAsRead() {
        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationReadAllResponse response = notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", response, requestId));
    }

    /**
     * COMM-NOT-005: Notification Summary
     */
    @GetMapping("/summary")
    @Operation(summary = "COMM-NOT-005: Notification Summary", description = "Get unread and total notification counts for dashboard/badges")
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> getSummary() {
        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationSummaryResponse response = notificationService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok("Notification summary retrieved", response, requestId));
    }

    /**
     * COMM-NOT-006: Notification History
     */
    @GetMapping("/history")
    @Operation(summary = "COMM-NOT-006: Notification History", description = "Get notification history for current user with filtering")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getHistory(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sourceService,
            @RequestParam(required = false) String sourceEntityType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String requestId = SecurityUtils.getCurrentRequestId();
        Page<NotificationResponse> resultPage = notificationService.getHistory(
                type, sourceService, sourceEntityType, priority, status, dateFrom, dateTo, page, size
        );

        PaginationDto pagination = PaginationDto.fromPage(resultPage);
        return ResponseEntity.ok(ApiResponse.ok("Notification history retrieved successfully", resultPage.getContent(), pagination, requestId));
    }

    /**
     * COMM-NOT-007: Archive Notification
     */
    @PostMapping("/{notificationId}/archive")
    @Operation(summary = "COMM-NOT-007: Archive Notification", description = "Archive notification without deletion")
    public ResponseEntity<ApiResponse<NotificationResponse>> archiveNotification(@PathVariable String notificationId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        NotificationResponse response = notificationService.archiveNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification archived successfully", response, requestId));
    }
}
