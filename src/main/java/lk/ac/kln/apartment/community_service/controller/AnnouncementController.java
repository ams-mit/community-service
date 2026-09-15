package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.AnnouncementRequest;
import lk.ac.kln.apartment.community_service.dto.AnnouncementResponse;
import lk.ac.kln.apartment.community_service.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse publishAnnouncement(@RequestBody AnnouncementRequest request) {
        return announcementService.publishAnnouncement(request);
    }

    @GetMapping
    public List<AnnouncementResponse> getAnnouncements(@RequestParam String role) {
        return announcementService.getAnnouncementsForRole(role);
    }
}