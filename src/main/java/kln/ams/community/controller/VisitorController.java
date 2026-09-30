package kln.ams.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kln.ams.community.dto.*;
import kln.ams.community.entity.VisitorStatus;
import kln.ams.community.security.SecurityUtils;
import kln.ams.community.service.VisitorService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/visitors")
@Tag(name = "Visitor Management", description = "Canonical visitor endpoints for Project A Community Service")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    /**
     * COMM-VIS-001: Create Visitor Request
     */
    @PostMapping
    @Operation(summary = "COMM-VIS-001: Create Visitor Request", description = "Create a visitor request associated with a valid resident/unit relationship")
    public ResponseEntity<ApiResponse<VisitorResponse>> createVisitor(@Valid @RequestBody VisitorRequest request) {
        String requestId = SecurityUtils.getCurrentRequestId();
        VisitorResponse response = visitorService.createVisitor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Visitor request created successfully", response, requestId));
    }

    /**
     * COMM-VIS-002: List Visitors
     */
    @GetMapping
    @Operation(summary = "COMM-VIS-002: List Visitors", description = "List visitor records with role and relationship based filtering and pagination")
    public ResponseEntity<ApiResponse<List<VisitorResponse>>> listVisitors(
            @RequestParam(required = false) String unitId,
            @RequestParam(required = false) String residentUserId,
            @RequestParam(required = false) VisitorStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String visitorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String requestId = SecurityUtils.getCurrentRequestId();
        Page<VisitorResponse> visitorPage = visitorService.listVisitors(
                unitId, residentUserId, status, visitDate, dateFrom, dateTo, visitorName, page, size
        );

        PaginationDto pagination = PaginationDto.fromPage(visitorPage);
        return ResponseEntity.ok(ApiResponse.ok("Visitors retrieved successfully", visitorPage.getContent(), pagination, requestId));
    }

    /**
     * COMM-VIS-003: Get Visitor
     */
    @GetMapping("/{visitorId}")
    @Operation(summary = "COMM-VIS-003: Get Visitor", description = "Get single visitor record and current visit state")
    public ResponseEntity<ApiResponse<VisitorResponse>> getVisitor(@PathVariable String visitorId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        VisitorResponse response = visitorService.getVisitorById(visitorId);
        return ResponseEntity.ok(ApiResponse.ok("Visitor retrieved successfully", response, requestId));
    }

    /**
     * COMM-VIS-004: Update Visitor Request
     */
    @PatchMapping("/{visitorId}")
    @Operation(summary = "COMM-VIS-004: Update Visitor Request", description = "Update editable fields of an active visitor request")
    public ResponseEntity<ApiResponse<VisitorResponse>> updateVisitor(
            @PathVariable String visitorId,
            @RequestBody VisitorUpdateRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        VisitorResponse response = visitorService.updateVisitor(visitorId, request);
        return ResponseEntity.ok(ApiResponse.ok("Visitor updated successfully", response, requestId));
    }

    /**
     * COMM-VIS-005: Approve Visitor
     */
    @PostMapping("/{visitorId}/approve")
    @Operation(summary = "COMM-VIS-005: Approve Visitor", description = "Security/Manager approval for pending visitor request")
    public ResponseEntity<ApiResponse<VisitorResponse>> approveVisitor(
            @PathVariable String visitorId,
            @RequestBody(required = false) VisitorStatusRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        String reason = request != null ? request.getReason() : "Approved";
        VisitorResponse response = visitorService.approveVisitor(visitorId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Visitor approved successfully", response, requestId));
    }

    /**
     * COMM-VIS-006: Reject Visitor
     */
    @PostMapping("/{visitorId}/reject")
    @Operation(summary = "COMM-VIS-006: Reject Visitor", description = "Security/Manager rejection with mandatory reason")
    public ResponseEntity<ApiResponse<VisitorResponse>> rejectVisitor(
            @PathVariable String visitorId,
            @Valid @RequestBody VisitorStatusRequest request) {

        String requestId = SecurityUtils.getCurrentRequestId();
        String reason = request != null ? request.getReason() : null;
        VisitorResponse response = visitorService.rejectVisitor(visitorId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Visitor rejected successfully", response, requestId));
    }

    /**
     * COMM-VIS-007: Check In Visitor
     */
    @PostMapping("/{visitorId}/check-in")
    @Operation(summary = "COMM-VIS-007: Check In Visitor", description = "Check-in approved visitor with server-generated timestamp")
    public ResponseEntity<ApiResponse<VisitorResponse>> checkInVisitor(@PathVariable String visitorId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        VisitorResponse response = visitorService.checkInVisitor(visitorId);
        return ResponseEntity.ok(ApiResponse.ok("Visitor checked in successfully", response, requestId));
    }

    /**
     * COMM-VIS-008: Check Out Visitor
     */
    @PostMapping("/{visitorId}/check-out")
    @Operation(summary = "COMM-VIS-008: Check Out Visitor", description = "Check-out checked-in visitor with server-generated timestamp")
    public ResponseEntity<ApiResponse<VisitorResponse>> checkOutVisitor(@PathVariable String visitorId) {
        String requestId = SecurityUtils.getCurrentRequestId();
        VisitorResponse response = visitorService.checkOutVisitor(visitorId);
        return ResponseEntity.ok(ApiResponse.ok("Visitor checked out successfully", response, requestId));
    }
}
