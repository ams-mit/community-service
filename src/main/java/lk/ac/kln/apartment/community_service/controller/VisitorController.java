package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.VisitorRequest;
import lk.ac.kln.apartment.community_service.dto.VisitorResponse;
import lk.ac.kln.apartment.community_service.service.VisitorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
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
}