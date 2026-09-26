package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.AnnouncementRequest;
import lk.ac.kln.apartment.community_service.dto.AnnouncementResponse;
import lk.ac.kln.apartment.community_service.entity.Announcement;
import lk.ac.kln.apartment.community_service.entity.AnnouncementStatus;
import lk.ac.kln.apartment.community_service.repository.AnnouncementRepository;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;

    @Autowired
    public AnnouncementService(AnnouncementRepository announcementRepository, NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.notificationService = notificationService;
    }

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this(announcementRepository, null);
    }

    public List<AnnouncementResponse> getAnnouncements(String role) {
        List<Announcement> announcements;
        if (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role.trim())) {
            announcements = announcementRepository.findByStatusOrderByCreatedAtDesc(AnnouncementStatus.ACTIVE);
        } else {
            announcements = announcementRepository.findActiveByRole(role.trim(), AnnouncementStatus.ACTIVE);
        }
        return announcements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AnnouncementResponse getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found with id: " + id));
        return mapToResponse(announcement);
    }

    @Transactional
    public AnnouncementResponse publishAnnouncement(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetRole(request.getTargetRole() != null ? request.getTargetRole() : "ALL");
        announcement.setPublishedBy(request.getPublishedBy() != null ? request.getPublishedBy() : "Management Office");
        announcement.setCategory(request.getCategory() != null ? request.getCategory() : "NOTICE");
        announcement.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        announcement.setExpiryDate(request.getExpiryDate());
        announcement.setAttachmentUrl(request.getAttachmentUrl());
        announcement.setStatus(AnnouncementStatus.ACTIVE);

        Announcement saved = announcementRepository.save(announcement);

        if (notificationService != null) {
            try {
                NotificationRequest notif = new NotificationRequest();
                notif.setTitle("New Announcement: " + saved.getTitle());
                String summary = saved.getContent().length() > 150 ? saved.getContent().substring(0, 147) + "..." : saved.getContent();
                notif.setSummary(summary);
                notif.setMessage(saved.getContent());
                notif.setType("announcement");
                notif.setCategory("BULLETIN");
                notif.setPriority(saved.getPriority() != null ? saved.getPriority().toLowerCase() : "normal");
                notif.setRecipientRole(saved.getTargetRole());
                notif.setIssuedBy(saved.getPublishedBy());
                notif.setActionRoute("/announcements");
                notif.setActionLabel("View Bulletin");
                notificationService.createNotification(notif);
            } catch (Exception ignored) {
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found with id: " + id));

        if (request.getTitle() != null) announcement.setTitle(request.getTitle());
        if (request.getContent() != null) announcement.setContent(request.getContent());
        if (request.getTargetRole() != null) announcement.setTargetRole(request.getTargetRole());
        if (request.getPublishedBy() != null) announcement.setPublishedBy(request.getPublishedBy());
        if (request.getCategory() != null) announcement.setCategory(request.getCategory());
        if (request.getPriority() != null) announcement.setPriority(request.getPriority());
        if (request.getExpiryDate() != null) announcement.setExpiryDate(request.getExpiryDate());
        if (request.getAttachmentUrl() != null) announcement.setAttachmentUrl(request.getAttachmentUrl());

        Announcement updated = announcementRepository.save(announcement);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found with id: " + id));
        announcementRepository.delete(announcement);
    }

    @Transactional
    public AnnouncementResponse archiveAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found with id: " + id));

        announcement.setStatus(AnnouncementStatus.ARCHIVED);
        Announcement updated = announcementRepository.save(announcement);
        return mapToResponse(updated);
    }

    public AnnouncementResponse mapToResponse(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getTargetRole(),
                announcement.getPublishedBy(),
                announcement.getStatus(),
                announcement.getCategory(),
                announcement.getPriority(),
                announcement.getExpiryDate(),
                announcement.getAttachmentUrl(),
                announcement.getCreatedAt()
        );
    }
}