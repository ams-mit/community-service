package kln.ams.community.service;

import kln.ams.community.dto.InternalNotificationRequest;
import kln.ams.community.dto.NotificationReadAllResponse;
import kln.ams.community.dto.NotificationResponse;
import kln.ams.community.dto.NotificationSummaryResponse;
import kln.ams.community.entity.Notification;
import kln.ams.community.entity.NotificationStatus;
import kln.ams.community.repository.NotificationRepository;
import kln.ams.community.security.JwtAuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("user-1", "user", Collections.emptyList()));
        request.setAttribute("requestId", "req-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createInternalNotification_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("operations-service", "service", Collections.emptyList()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        InternalNotificationRequest req = InternalNotificationRequest.builder()
                .recipientUserId("user-1")
                .type("BOOKING_APPROVED")
                .title("Booking Approved")
                .message("Your booking has been approved.")
                .sourceService("operations-service")
                .sourceEntityType("Booking")
                .sourceEntityId("book-1")
                .build();

        when(notificationRepository.findFirstBySourceServiceAndSourceEntityTypeAndSourceEntityIdAndTypeAndRecipientUserId(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId("notif-1");
            return n;
        });

        NotificationResponse res = notificationService.createInternalNotification(req);
        assertNotNull(res);
        assertEquals("notif-1", res.getId());
        assertEquals(NotificationStatus.UNREAD, res.getStatus());
        assertEquals("DELIVERED", res.getDeliveryStatus());
    }

    @Test
    void createInternalNotification_idempotent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("operations-service", "service", Collections.emptyList()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        InternalNotificationRequest req = InternalNotificationRequest.builder()
                .recipientUserId("user-1")
                .type("BOOKING_APPROVED")
                .title("Booking Approved")
                .message("Your booking has been approved.")
                .sourceService("operations-service")
                .sourceEntityType("Booking")
                .sourceEntityId("book-1")
                .build();

        Notification existing = Notification.builder()
                .id("notif-existing")
                .recipientUserId("user-1")
                .type("BOOKING_APPROVED")
                .title("Booking Approved")
                .message("Your booking has been approved.")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationRepository.findFirstBySourceServiceAndSourceEntityTypeAndSourceEntityIdAndTypeAndRecipientUserId(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(existing));

        NotificationResponse res = notificationService.createInternalNotification(req);
        assertEquals("notif-existing", res.getId());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_success() {
        Notification notification = Notification.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        NotificationResponse res = notificationService.markAsRead("notif-1");
        assertEquals(NotificationStatus.READ, res.getStatus());
        assertNotNull(res.getReadAt());
    }

    @Test
    void markAllAsRead_success() {
        Notification n1 = Notification.builder().id("1").recipientUserId("user-1").status(NotificationStatus.UNREAD).build();
        Notification n2 = Notification.builder().id("2").recipientUserId("user-1").status(NotificationStatus.UNREAD).build();

        when(notificationRepository.findByRecipientUserIdAndStatus("user-1", NotificationStatus.UNREAD))
                .thenReturn(List.of(n1, n2));

        NotificationReadAllResponse res = notificationService.markAllAsRead();
        assertEquals(2, res.getUpdatedCount());
        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    void getSummary_returnsCorrectCounts() {
        when(notificationRepository.countByRecipientUserIdAndStatus("user-1", NotificationStatus.UNREAD)).thenReturn(3L);
        when(notificationRepository.countByRecipientUserId("user-1")).thenReturn(10L);

        NotificationSummaryResponse res = notificationService.getSummary();
        assertEquals(3L, res.getUnreadCount());
        assertEquals(10L, res.getTotalCount());
    }
}
