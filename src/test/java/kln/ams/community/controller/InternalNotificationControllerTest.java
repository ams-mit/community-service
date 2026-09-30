package kln.ams.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kln.ams.community.dto.InternalNotificationRequest;
import kln.ams.community.dto.InternalResidentNotificationRequest;
import kln.ams.community.dto.NotificationResponse;
import kln.ams.community.dto.NotificationStatusResponse;
import kln.ams.community.entity.NotificationStatus;
import kln.ams.community.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InternalNotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InternalNotificationController internalNotificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(internalNotificationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createInternalNotification_returnsCreated() throws Exception {
        InternalNotificationRequest req = InternalNotificationRequest.builder()
                .recipientUserId("user-1")
                .type("BOOKING_APPROVED")
                .title("Booking Approved")
                .message("Your booking was approved")
                .sourceService("operations-service")
                .sourceEntityType("Booking")
                .sourceEntityId("book-1")
                .build();

        NotificationResponse res = NotificationResponse.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .type("BOOKING_APPROVED")
                .title("Booking Approved")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.createInternalNotification(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/internal/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("notif-1"));
    }

    @Test
    void getInternalNotificationStatus_returnsStatus() throws Exception {
        NotificationStatusResponse res = NotificationStatusResponse.builder()
                .notificationId("notif-1")
                .status(NotificationStatus.UNREAD)
                .deliveryStatus("DELIVERED")
                .build();

        when(notificationService.getInternalNotificationStatus(eq("notif-1"))).thenReturn(res);

        mockMvc.perform(get("/api/v1/internal/notifications/notif-1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value("notif-1"))
                .andExpect(jsonPath("$.data.deliveryStatus").value("DELIVERED"));
    }

    @Test
    void createInternalResidentNotification_returnsCreated() throws Exception {
        InternalResidentNotificationRequest req = InternalResidentNotificationRequest.builder()
                .residentUserId("user-1")
                .type("MAINTENANCE_REQUEST_UPDATED")
                .title("Request Updated")
                .message("Request in progress")
                .sourceService("operations-service")
                .sourceEntityType("MaintenanceRequest")
                .sourceEntityId("req-1")
                .build();

        NotificationResponse res = NotificationResponse.builder()
                .id("notif-2")
                .recipientUserId("user-1")
                .type("MAINTENANCE_REQUEST_UPDATED")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.createInternalResidentNotification(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/internal/notifications/resident")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("notif-2"));
    }
}
