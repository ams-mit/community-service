package kln.ams.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kln.ams.community.dto.*;
import kln.ams.community.entity.AnnouncementStatus;
import kln.ams.community.entity.AudienceType;
import kln.ams.community.security.SecurityUtils;
import kln.ams.community.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@Tag(name = "Announcement Management", description = "Canonical announcement endpoints for Project A Community Service")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * COMM-ANN-001: Create Announcement
     */
    @PostMapping
    @Operation(summary = "COMM-ANN-001: Create Announcement", description = "Create announcement in DRAFT status with target audience")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementResponse response = announcementService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Announcement created successfully", response, requestId));
    }

    /**
     * COMM-ANN-002: List Announcements
     */
    @GetMapping
    @Operation(summary = "COMM-ANN-002: List Announcements", description = "List announcements with audience-based filtering for residents")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> listAnnouncements(
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) AudienceType audienceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String requestId = SecurityUtils.getCurrentRequestId();
        Page<AnnouncementResponse> pageResult = announcementService.listAnnouncements(
                status, category, priority, audienceType, publishedFrom, publishedTo, page, size
        );

        PaginationDto pagination = PaginationDto.fromPage(pageResult);
        return ResponseEntity.ok(ApiResponse.ok("Announcements retrieved successfully", pageResult.getContent(), pagination, requestId));
    }

    /**
     * COMM-ANN-003: Get Announcement
     */
    @GetMapping("/{announcementId}")
    @Operation(summary = "COMM-ANN-003: Get Announcement", description = "Get announcement by ID with audience visibility check")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncement(@PathVariable String announcementId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementResponse response = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(ApiResponse.ok("Announcement retrieved successfully", response, requestId));
    }

    /**
     * COMM-ANN-004: Update Announcement
     */
    @PatchMapping("/{announcementId}")
    @Operation(summary = "COMM-ANN-004: Update Announcement", description = "Update announcement fields before/after publication")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable String announcementId,
            @RequestBody AnnouncementUpdateRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementResponse response = announcementService.updateAnnouncement(announcementId, request);
        return ResponseEntity.ok(ApiResponse.ok("Announcement updated successfully", response, requestId));
    }

    /**
     * COMM-ANN-005: Publish Announcement
     */
    @PostMapping("/{announcementId}/publish")
    @Operation(summary = "COMM-ANN-005: Publish Announcement", description = "Publish announcement to its configured audience with server-controlled timestamp")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> publishAnnouncement(@PathVariable String announcementId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementResponse response = announcementService.publishAnnouncement(announcementId);
        return ResponseEntity.ok(ApiResponse.ok("Announcement published successfully", response, requestId));
    }

    /**
     * COMM-ANN-006: Cancel Announcement
     */
    @PostMapping("/{announcementId}/cancel")
    @Operation(summary = "COMM-ANN-006: Cancel Announcement", description = "Cancel announcement with mandatory reason")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> cancelAnnouncement(
            @PathVariable String announcementId,
            @Valid @RequestBody AnnouncementCancelRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementResponse response = announcementService.cancelAnnouncement(announcementId, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Announcement cancelled successfully", response, requestId));
    }

    /**
     * COMM-ANN-007: Mark Announcement Read
     */
    @PostMapping("/{announcementId}/read")
    @Operation(summary = "COMM-ANN-007: Mark Announcement Read", description = "Record that current authenticated user has read this announcement")
    public ResponseEntity<ApiResponse<AnnouncementReadResponse>> markRead(@PathVariable String announcementId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        AnnouncementReadResponse response = announcementService.recordRead(announcementId);
        return ResponseEntity.ok(ApiResponse.ok("Announcement marked as read", response, requestId));
    }

    /**
     * COMM-ANN-008: Announcement Audience Preview
     */
    @GetMapping("/{announcementId}/audience-preview")
    @Operation(summary = "COMM-ANN-008: Announcement Audience Preview", description = "Show management the effective audience summary")
    public ResponseEntity<ApiResponse<AudiencePreviewResponse>> getAudiencePreview(@PathVariable String announcementId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        AudiencePreviewResponse response = announcementService.getAudiencePreview(announcementId);
        return ResponseEntity.ok(ApiResponse.ok("Audience preview retrieved successfully", response, requestId));
    }
}
