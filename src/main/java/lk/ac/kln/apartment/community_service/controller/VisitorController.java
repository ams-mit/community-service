package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.VisitorRequest;
import lk.ac.kln.apartment.community_service.dto.VisitorResponse;
import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import lk.ac.kln.apartment.community_service.service.VisitorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public List<VisitorResponse> getVisitors(
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) VisitorStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        return visitorService.getVisitors(residentId, status, visitDate);
    }

    @GetMapping("/{visitorId}")
    public VisitorResponse getVisitorById(@PathVariable Long visitorId) {
        return visitorService.getVisitorById(visitorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitorResponse registerVisitor(@RequestBody VisitorRequest request) {
        return visitorService.registerVisitor(request);
    }

    @PatchMapping("/{visitorId}/check-in")
    public VisitorResponse checkInVisitor(@PathVariable Long visitorId) {
        return visitorService.checkInVisitor(visitorId);
    }

    @PatchMapping("/{visitorId}/check-out")
    public VisitorResponse checkOutVisitor(@PathVariable Long visitorId) {
        return visitorService.checkOutVisitor(visitorId);
    }

    @PatchMapping("/{visitorId}/cancel")
    public VisitorResponse cancelVisitor(@PathVariable Long visitorId) {
        return visitorService.cancelVisitor(visitorId);
    }

    @GetMapping("/verify/{passCode}")
    public VisitorResponse verifyPassCode(@PathVariable String passCode) {
        return visitorService.verifyPassCode(passCode);
    }
}