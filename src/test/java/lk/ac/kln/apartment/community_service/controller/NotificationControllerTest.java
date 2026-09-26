package lk.ac.kln.apartment.community_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import lk.ac.kln.apartment.community_service.dto.NotificationResponse;
import lk.ac.kln.apartment.community_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createNotification_returnsCreated_COMM_008() throws Exception {
        NotificationRequest request = new NotificationRequest(
                "resident-001",
                "RESIDENT",
                "Water Outage Notice",
                "Water maintenance tomorrow",
                "Detailed message",
                "facility",
                "FACILITY",
                "high",
                "Maintenance Dept",
                "Tower B",
                "/facilities",
                "View Details"
        );

        NotificationResponse response = new NotificationResponse(
                1L,
                "resident-001",
                "RESIDENT",
                "Water Outage Notice",
                "Water maintenance tomorrow",
                "Detailed message",
                "facility",
                "FACILITY",
                "high",
                "Maintenance Dept",
                "Tower B",
                "/facilities",
                "View Details",
                false,
                LocalDateTime.now(),
                null
        );

        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Water Outage Notice"))
                .andExpect(jsonPath("$.category").value("FACILITY"));
    }

    @Test
    void getNotifications_returnsList_COMM_009() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                "resident-001",
                "RESIDENT",
                "Gate Pass Issued",
                "Pass issued for visitor",
                "Pass details",
                "security",
                "SECURITY",
                "normal",
                "Security Gate",
                "Main Gate",
                "/visitors",
                "View Passes",
                false,
                LocalDateTime.now(),
                null
        );

        when(notificationService.getNotifications(eq("resident-001"), any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("recipientId", "resident-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Gate Pass Issued"))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void markAsRead_returnsUpdated_COMM_010() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                "resident-001",
                "RESIDENT",
                "Gate Pass Issued",
                "Pass issued for visitor",
                "Pass details",
                "security",
                "SECURITY",
                "normal",
                "Security Gate",
                "Main Gate",
                "/visitors",
                "View Passes",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(notificationService.markAsRead(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }
}
