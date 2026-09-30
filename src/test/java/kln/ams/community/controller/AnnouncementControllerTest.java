package kln.ams.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kln.ams.community.dto.AnnouncementCreateRequest;
import kln.ams.community.dto.AnnouncementReadResponse;
import kln.ams.community.dto.AnnouncementResponse;
import kln.ams.community.entity.AnnouncementStatus;
import kln.ams.community.entity.AudienceType;
import kln.ams.community.service.AnnouncementService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private AnnouncementController announcementController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(announcementController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createAnnouncement_returnsCreated() throws Exception {
        AnnouncementCreateRequest req = AnnouncementCreateRequest.builder()
                .title("Maintenance")
                .content("Water outage")
                .audienceType(AudienceType.ALL_RESIDENTS)
                .build();

        AnnouncementResponse res = AnnouncementResponse.builder()
                .id("ann-1")
                .title("Maintenance")
                .status(AnnouncementStatus.DRAFT)
                .audienceType(AudienceType.ALL_RESIDENTS)
                .build();

        when(announcementService.createAnnouncement(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ann-1"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void publishAnnouncement_returnsPublished() throws Exception {
        AnnouncementResponse res = AnnouncementResponse.builder()
                .id("ann-1")
                .status(AnnouncementStatus.PUBLISHED)
                .build();

        when(announcementService.publishAnnouncement(eq("ann-1"))).thenReturn(res);

        mockMvc.perform(post("/api/v1/announcements/ann-1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void markRead_returnsSuccess() throws Exception {
        AnnouncementReadResponse res = AnnouncementReadResponse.builder()
                .announcementId("ann-1")
                .userId("user-1")
                .readAt(Instant.now())
                .build();

        when(announcementService.recordRead(eq("ann-1"))).thenReturn(res);

        mockMvc.perform(post("/api/v1/announcements/ann-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.announcementId").value("ann-1"));
    }
}
