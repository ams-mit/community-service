package kln.ams.community.service;

import kln.ams.community.dto.AnnouncementCancelRequest;
import kln.ams.community.dto.AnnouncementCreateRequest;
import kln.ams.community.dto.AnnouncementResponse;
import kln.ams.community.entity.Announcement;
import kln.ams.community.entity.AnnouncementStatus;
import kln.ams.community.entity.AudienceType;
import kln.ams.community.exception.BusinessRuleViolationException;
import kln.ams.community.repository.AnnouncementAudienceRepository;
import kln.ams.community.repository.AnnouncementReadRepository;
import kln.ams.community.repository.AnnouncementRepository;
import kln.ams.community.security.JwtAuthPrincipal;
import kln.ams.community.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private AnnouncementAudienceRepository audienceRepository;

    @Mock
    private AnnouncementReadRepository readRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("admin-1", "user", List.of(RoleConstants.APARTMENT_MANAGER)));
        request.setAttribute("requestId", "req-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createAnnouncement_defaultsToDraft() {
        AnnouncementCreateRequest req = AnnouncementCreateRequest.builder()
                .title("Power Maintenance")
                .content("Power outage planned")
                .audienceType(AudienceType.ALL_RESIDENTS)
                .build();

        when(announcementRepository.save(any(Announcement.class))).thenAnswer(i -> {
            Announcement a = i.getArgument(0);
            a.setId("ann-1");
            return a;
        });

        AnnouncementResponse res = announcementService.createAnnouncement(req);
        assertNotNull(res);
        assertEquals(AnnouncementStatus.DRAFT, res.getStatus());
        assertEquals("Power Maintenance", res.getTitle());
    }

    @Test
    void publishAnnouncement_success() {
        Announcement announcement = Announcement.builder()
                .id("ann-1")
                .title("Notice")
                .content("Details")
                .status(AnnouncementStatus.DRAFT)
                .audienceType(AudienceType.ALL_RESIDENTS)
                .build();

        when(announcementRepository.findById("ann-1")).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AnnouncementResponse res = announcementService.publishAnnouncement("ann-1");
        assertEquals(AnnouncementStatus.PUBLISHED, res.getStatus());
        assertNotNull(res.getPublishedAt());
    }

    @Test
    void cancelAnnouncement_requiresReason() {
        assertThrows(BusinessRuleViolationException.class, () -> announcementService.cancelAnnouncement("ann-1", ""));
    }
}
