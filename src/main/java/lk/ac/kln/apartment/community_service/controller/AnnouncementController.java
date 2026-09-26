package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.AnnouncementRequest;
import lk.ac.kln.apartment.community_service.dto.AnnouncementResponse;
import lk.ac.kln.apartment.community_service.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<AnnouncementResponse> getAnnouncements(@RequestParam(required = false) String role) {
        return announcementService.getAnnouncements(role);
    }

    @GetMapping("/{id}")
    public AnnouncementResponse getAnnouncementById(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementResponse publishAnnouncement(@RequestBody AnnouncementRequest request) {
        return announcementService.publishAnnouncement(request);
    }

    @PutMapping("/{id}")
    public AnnouncementResponse updateAnnouncement(
            @PathVariable Long id,
            @RequestBody AnnouncementRequest request) {
        return announcementService.updateAnnouncement(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public AnnouncementResponse archiveAnnouncement(@PathVariable Long id) {
        return announcementService.archiveAnnouncement(id);
    }
}