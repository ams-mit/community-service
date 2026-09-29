package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.AnnouncementRequest;
import lk.ac.kln.apartment.community_service.dto.AnnouncementResponse;
import lk.ac.kln.apartment.community_service.entity.Announcement;
import lk.ac.kln.apartment.community_service.entity.AnnouncementStatus;
import lk.ac.kln.apartment.community_service.repository.AnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    void getAnnouncements_returnsAllActive_whenRoleIsAllOrBlank() {
        Announcement forAll = new Announcement();
        forAll.setId(1L);
        forAll.setTitle("AGM");
        forAll.setTargetRole("ALL");
        forAll.setStatus(AnnouncementStatus.ACTIVE);
        forAll.setCreatedAt(LocalDateTime.now());

        when(announcementRepository.findByStatusOrderByCreatedAtDesc(AnnouncementStatus.ACTIVE))
                .thenReturn(List.of(forAll));

        List<AnnouncementResponse> result = announcementService.getAnnouncements("ALL");

        assertEquals(1, result.size());
    }

    @Test
    void getAnnouncements_returnsRoleSpecific_whenRoleGiven() {
        Announcement tenantOnly = new Announcement();
        tenantOnly.setId(2L);
        tenantOnly.setTitle("Water Maintenance");
        tenantOnly.setTargetRole("TENANT");
        tenantOnly.setStatus(AnnouncementStatus.ACTIVE);
        tenantOnly.setCreatedAt(LocalDateTime.now());

        when(announcementRepository.findActiveByRole("TENANT", AnnouncementStatus.ACTIVE))
                .thenReturn(List.of(tenantOnly));

        List<AnnouncementResponse> result = announcementService.getAnnouncements("TENANT");

        assertEquals(1, result.size());
        assertEquals("TENANT", result.get(0).getTargetRole());
    }

    @Test
    void publishAnnouncement_savesWithActiveStatus() {
        AnnouncementRequest request = new AnnouncementRequest(
                "Title", "Content", "TENANT", "manager-001",
                "NOTICE", "MEDIUM", null, null
        );

        when(announcementRepository.save(any(Announcement.class))).thenAnswer(invocation -> {
            Announcement a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        AnnouncementResponse response = announcementService.publishAnnouncement(request);

        assertEquals(AnnouncementStatus.ACTIVE, response.getStatus());
        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    @Test
    void archiveAnnouncement_setsArchivedStatus() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setStatus(AnnouncementStatus.ACTIVE);

        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnnouncementResponse response = announcementService.archiveAnnouncement(1L);

        assertEquals(AnnouncementStatus.ARCHIVED, response.getStatus());
    }
}