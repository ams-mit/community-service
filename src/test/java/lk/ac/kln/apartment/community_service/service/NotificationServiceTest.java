package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import lk.ac.kln.apartment.community_service.dto.NotificationResponse;
import lk.ac.kln.apartment.community_service.entity.Notification;
import lk.ac.kln.apartment.community_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = new Notification();
        sampleNotification.setId(1L);
        sampleNotification.setRecipientId("resident-001");
        sampleNotification.setRecipientRole("RESIDENT");
        sampleNotification.setTitle("Fire Alarm Drill");
        sampleNotification.setSummary("Drill scheduled for Saturday");
        sampleNotification.setMessage("Comprehensive annual drill details");
        sampleNotification.setType("maintenance");
        sampleNotification.setCategory("MAINTENANCE");
        sampleNotification.setPriority("high");
        sampleNotification.setIssuedBy("Central Safety");
        sampleNotification.setRead(false);
        sampleNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_success() {
        NotificationRequest request = new NotificationRequest(
                "resident-001",
                "RESIDENT",
                "Fire Alarm Drill",
                "Drill scheduled for Saturday",
                "Comprehensive annual drill details",
                "maintenance",
                "MAINTENANCE",
                "high",
                "Central Safety",
                "Tower A",
                "/announcements",
                "View Details"
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        });

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Fire Alarm Drill", response.getTitle());
        assertEquals("maintenance", response.getType());
        assertEquals("MAINTENANCE", response.getCategory());
        assertFalse(response.isRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotifications_filtersCorrectly() {
        when(notificationRepository.findFiltered("resident-001", "RESIDENT", true))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> result = notificationService.getNotifications("resident-001", "RESIDENT", true);

        assertEquals(1, result.size());
        assertEquals("Fire Alarm Drill", result.get(0).getTitle());
        verify(notificationRepository, times(1)).findFiltered("resident-001", "RESIDENT", true);
    }

    @Test
    void markAsRead_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertTrue(response.isRead());
        assertNotNull(response.getReadAt());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    void markAsRead_throwsNotFound_whenNonExistent() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.markAsRead(999L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void markAllAsRead_returnsCount() {
        when(notificationRepository.markAllAsRead("resident-001")).thenReturn(3);

        int updated = notificationService.markAllAsRead("resident-001");

        assertEquals(3, updated);
        verify(notificationRepository, times(1)).markAllAsRead("resident-001");
    }
}
