package kln.ams.community.service;

import kln.ams.community.dto.VisitorRequest;
import kln.ams.community.dto.VisitorResponse;
import kln.ams.community.entity.Visitor;
import kln.ams.community.entity.VisitorStatus;
import kln.ams.community.exception.BusinessRuleViolationException;
import kln.ams.community.exception.DuplicateResourceException;
import kln.ams.community.exception.InvalidStatusTransitionException;
import kln.ams.community.repository.VisitorHistoryRepository;
import kln.ams.community.repository.VisitorRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest {

    @Mock
    private VisitorRepository visitorRepository;

    @Mock
    private VisitorHistoryRepository visitorHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private VisitorService visitorService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("user-1", "user", List.of(RoleConstants.TENANT_RESIDENT)));
        request.setAttribute("requestId", "req-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createVisitor_success() {
        VisitorRequest req = VisitorRequest.builder()
                .unitId("unit-1")
                .visitorName("John Doe")
                .visitorContact("0771234567")
                .visitDate(LocalDate.now().plusDays(1))
                .expectedArrival("10:00")
                .expectedDeparture("12:00")
                .purpose("Meeting")
                .build();

        when(visitorRepository.existsByUnitIdAndVisitorNameIgnoreCaseAndVisitDateAndStatusIn(any(), any(), any(), any()))
                .thenReturn(false);
        when(visitorRepository.save(any(Visitor.class))).thenAnswer(i -> {
            Visitor v = i.getArgument(0);
            v.setId("vis-123");
            return v;
        });

        VisitorResponse res = visitorService.createVisitor(req);
        assertNotNull(res);
        assertEquals("vis-123", res.getId());
        assertEquals(VisitorStatus.PENDING, res.getStatus());
        verify(visitorHistoryRepository, times(1)).save(any());
    }

    @Test
    void createVisitor_duplicateThrowsException() {
        VisitorRequest req = VisitorRequest.builder()
                .unitId("unit-1")
                .visitorName("John Doe")
                .visitDate(LocalDate.now().plusDays(1))
                .purpose("Meeting")
                .build();

        when(visitorRepository.existsByUnitIdAndVisitorNameIgnoreCaseAndVisitDateAndStatusIn(any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> visitorService.createVisitor(req));
    }

    @Test
    void createVisitor_invalidTimesThrowsException() {
        VisitorRequest req = VisitorRequest.builder()
                .unitId("unit-1")
                .visitorName("John Doe")
                .visitDate(LocalDate.now().plusDays(1))
                .expectedArrival("14:00")
                .expectedDeparture("12:00")
                .purpose("Meeting")
                .build();

        assertThrows(BusinessRuleViolationException.class, () -> visitorService.createVisitor(req));
    }

    @Test
    void approveVisitor_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("sec-1", "user", List.of(RoleConstants.SECURITY_OFFICER)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Visitor visitor = Visitor.builder()
                .id("vis-1")
                .residentUserId("user-1")
                .visitorName("John Doe")
                .status(VisitorStatus.PENDING)
                .build();

        when(visitorRepository.findById("vis-1")).thenReturn(Optional.of(visitor));
        when(visitorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VisitorResponse res = visitorService.approveVisitor("vis-1", "Verified");
        assertEquals(VisitorStatus.APPROVED, res.getStatus());
        verify(notificationService, times(1)).createInternalNotificationDirect(any(), eq("VISITOR_APPROVED"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectVisitor_requiresReason() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("sec-1", "user", List.of(RoleConstants.SECURITY_OFFICER)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThrows(BusinessRuleViolationException.class, () -> visitorService.rejectVisitor("vis-1", ""));
    }

    @Test
    void checkInVisitor_requiresApproved() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("sec-1", "user", List.of(RoleConstants.SECURITY_OFFICER)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Visitor visitor = Visitor.builder()
                .id("vis-1")
                .status(VisitorStatus.PENDING)
                .build();

        when(visitorRepository.findById("vis-1")).thenReturn(Optional.of(visitor));

        assertThrows(InvalidStatusTransitionException.class, () -> visitorService.checkInVisitor("vis-1"));
    }

    @Test
    void checkOutVisitor_requiresCheckedIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("jwtPrincipal", new JwtAuthPrincipal("sec-1", "user", List.of(RoleConstants.SECURITY_OFFICER)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Visitor visitor = Visitor.builder()
                .id("vis-1")
                .status(VisitorStatus.APPROVED)
                .build();

        when(visitorRepository.findById("vis-1")).thenReturn(Optional.of(visitor));

        assertThrows(InvalidStatusTransitionException.class, () -> visitorService.checkOutVisitor("vis-1"));
    }
}
