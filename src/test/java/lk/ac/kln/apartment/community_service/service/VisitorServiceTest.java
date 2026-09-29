package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.VisitorRequest;
import lk.ac.kln.apartment.community_service.dto.VisitorResponse;
import lk.ac.kln.apartment.community_service.entity.Visitor;
import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import lk.ac.kln.apartment.community_service.repository.VisitorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest {

    @Mock
    private VisitorRepository visitorRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private VisitorService visitorService;

    @Test
    void registerVisitor_success_defaultsToExpectedStatus() {
        VisitorRequest request = new VisitorRequest(
                "Kamal Perera", "resident-001", "unit-101",
                "Family visit", LocalDate.of(2026, 9, 20),
                "0771234567", "ABC-1234"
        );

        when(visitorRepository.save(any(Visitor.class))).thenAnswer(invocation -> {
            Visitor v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        VisitorResponse response = visitorService.registerVisitor(request);

        assertEquals(VisitorStatus.EXPECTED, response.getStatus());
        assertEquals("Kamal Perera", response.getVisitorName());
        assertNotNull(response.getPassCode());
        verify(visitorRepository, times(1)).save(any(Visitor.class));
    }

    @Test
    void checkInVisitor_success_whenCurrentlyExpected() {
        Visitor visitor = new Visitor();
        visitor.setId(1L);
        visitor.setResidentId("resident-001");
        visitor.setUnitId("unit-101");
        visitor.setVisitorName("Kamal Perera");
        visitor.setStatus(VisitorStatus.EXPECTED);

        when(visitorRepository.findById(1L)).thenReturn(Optional.of(visitor));
        when(visitorRepository.save(any(Visitor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisitorResponse response = visitorService.checkInVisitor(1L);

        assertEquals(VisitorStatus.CHECKED_IN, response.getStatus());
        assertNotNull(response.getCheckedInAt());
    }

    @Test
    void checkInVisitor_throwsBadRequest_whenAlreadyCheckedIn() {
        Visitor visitor = new Visitor();
        visitor.setId(1L);
        visitor.setStatus(VisitorStatus.CHECKED_IN);

        when(visitorRepository.findById(1L)).thenReturn(Optional.of(visitor));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> visitorService.checkInVisitor(1L)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(visitorRepository, never()).save(any());
    }

    @Test
    void checkInVisitor_throwsNotFound_whenVisitorDoesNotExist() {
        when(visitorRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> visitorService.checkInVisitor(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void checkOutVisitor_success_whenCurrentlyCheckedIn() {
        Visitor visitor = new Visitor();
        visitor.setId(2L);
        visitor.setStatus(VisitorStatus.CHECKED_IN);

        when(visitorRepository.findById(2L)).thenReturn(Optional.of(visitor));
        when(visitorRepository.save(any(Visitor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisitorResponse response = visitorService.checkOutVisitor(2L);

        assertEquals(VisitorStatus.CHECKED_OUT, response.getStatus());
        assertNotNull(response.getCheckedOutAt());
    }

    @Test
    void checkOutVisitor_throwsBadRequest_whenNotCheckedIn() {
        Visitor visitor = new Visitor();
        visitor.setId(2L);
        visitor.setStatus(VisitorStatus.EXPECTED);

        when(visitorRepository.findById(2L)).thenReturn(Optional.of(visitor));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> visitorService.checkOutVisitor(2L)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}