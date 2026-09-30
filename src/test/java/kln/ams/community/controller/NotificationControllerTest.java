package kln.ams.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kln.ams.community.dto.NotificationReadAllResponse;
import kln.ams.community.dto.NotificationResponse;
import kln.ams.community.dto.NotificationSummaryResponse;
import kln.ams.community.entity.NotificationStatus;
import kln.ams.community.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    void listMyNotifications_returnsPage() throws Exception {
        NotificationResponse res = NotificationResponse.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .title("Maintenance notice")
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationService.listMyNotifications(any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(res)));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("notif-1"));
    }

    @Test
    void markAsRead_returnsSuccess() throws Exception {
        NotificationResponse res = NotificationResponse.builder()
                .id("notif-1")
                .status(NotificationStatus.READ)
                .build();

        when(notificationService.markAsRead(eq("notif-1"))).thenReturn(res);

        mockMvc.perform(post("/api/v1/notifications/notif-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("READ"));
    }

    @Test
    void markAllAsRead_returnsCount() throws Exception {
        when(notificationService.markAllAsRead()).thenReturn(new NotificationReadAllResponse(5));

        mockMvc.perform(post("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.updatedCount").value(5));
    }

    @Test
    void getSummary_returnsCounts() throws Exception {
        when(notificationService.getSummary()).thenReturn(new NotificationSummaryResponse(2, 10));

        mockMvc.perform(get("/api/v1/notifications/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(10));
    }
}
