package kln.ams.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kln.ams.community.dto.VisitorRequest;
import kln.ams.community.dto.VisitorResponse;
import kln.ams.community.entity.VisitorStatus;
import kln.ams.community.service.VisitorService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VisitorControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private VisitorService visitorService;

    @InjectMocks
    private VisitorController visitorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(visitorController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createVisitor_returnsCreated() throws Exception {
        VisitorRequest req = VisitorRequest.builder()
                .unitId("unit-101")
                .visitorName("Nimal Perera")
                .visitorContact("0770000000")
                .visitDate(LocalDate.now().plusDays(1))
                .expectedArrival("10:00")
                .expectedDeparture("12:00")
                .purpose("Visit")
                .build();

        VisitorResponse res = VisitorResponse.builder()
                .id("vis-uuid-1")
                .unitId("unit-101")
                .visitorName("Nimal Perera")
                .status(VisitorStatus.PENDING)
                .build();

        when(visitorService.createVisitor(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/visitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("vis-uuid-1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void listVisitors_returnsListWithPagination() throws Exception {
        VisitorResponse res = VisitorResponse.builder()
                .id("vis-uuid-1")
                .visitorName("Nimal Perera")
                .status(VisitorStatus.PENDING)
                .build();

        when(visitorService.listVisitors(any(), any(), any(), any(), any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(res)));

        mockMvc.perform(get("/api/v1/visitors")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("vis-uuid-1"))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void approveVisitor_returnsApproved() throws Exception {
        VisitorResponse res = VisitorResponse.builder()
                .id("vis-uuid-1")
                .status(VisitorStatus.APPROVED)
                .build();

        when(visitorService.approveVisitor(eq("vis-uuid-1"), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/visitors/vis-uuid-1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Approved for visit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void checkInVisitor_returnsCheckedIn() throws Exception {
        VisitorResponse res = VisitorResponse.builder()
                .id("vis-uuid-1")
                .status(VisitorStatus.CHECKED_IN)
                .build();

        when(visitorService.checkInVisitor(eq("vis-uuid-1"))).thenReturn(res);

        mockMvc.perform(post("/api/v1/visitors/vis-uuid-1/check-in"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CHECKED_IN"));
    }

    @Test
    void checkOutVisitor_returnsCheckedOut() throws Exception {
        VisitorResponse res = VisitorResponse.builder()
                .id("vis-uuid-1")
                .status(VisitorStatus.CHECKED_OUT)
                .build();

        when(visitorService.checkOutVisitor(eq("vis-uuid-1"))).thenReturn(res);

        mockMvc.perform(post("/api/v1/visitors/vis-uuid-1/check-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CHECKED_OUT"));
    }
}
