package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.AnnouncementRequest;
import lk.ac.kln.apartment.community_service.dto.AnnouncementResponse;
import lk.ac.kln.apartment.community_service.entity.Announcement;
import lk.ac.kln.apartment.community_service.entity.AnnouncementStatus;
import lk.ac.kln.apartment.community_service.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public AnnouncementResponse publishAnnouncement(AnnouncementRequest request) {

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetRole(request.getTargetRole());
        announcement.setPublishedBy(request.getPublishedBy());
        announcement.setStatus(AnnouncementStatus.ACTIVE);

        Announcement saved = announcementRepository.save(announcement);

        return mapToResponse(saved);
    }

    public List<AnnouncementResponse> getAnnouncementsForRole(String callerRole) {

        List<Announcement> announcements = announcementRepository
                .findByTargetRoleInOrTargetRole(List.of(callerRole), "ALL");

        return announcements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AnnouncementResponse mapToResponse(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getTargetRole(),
                announcement.getPublishedBy(),
                announcement.getStatus(),
                announcement.getCreatedAt()
        );
    }
}