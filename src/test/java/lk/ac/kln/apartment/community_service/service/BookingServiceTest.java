package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.BookingRequest;
import lk.ac.kln.apartment.community_service.dto.BookingResponse;
import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lk.ac.kln.apartment.community_service.entity.Facility;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lk.ac.kln.apartment.community_service.repository.BookingRepository;
import lk.ac.kln.apartment.community_service.repository.FacilityRepository;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private BookingService bookingService;

    private Facility activeFacility;

    @BeforeEach
    void setUp() {
        activeFacility = new Facility();
        activeFacility.setId(1L);
        activeFacility.setName("Main Gym");
        activeFacility.setStatus(FacilityStatus.ACTIVE);
    }

    @Test
    void createBooking_success_whenNoConflictExists() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 12, 0),
                "resident-001",
                "RESIDENT"
        );

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(activeFacility));
        when(bookingRepository.findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            b.setCreatedAt(LocalDateTime.now());
            return b;
        });

        BookingResponse response = bookingService.createBooking(request);

        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals("Main Gym", response.getFacilityName());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsConflict_whenOverlappingApprovedBookingExists() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 12, 0),
                "resident-001",
                "RESIDENT"
        );

        Booking existingApproved = new Booking();
        existingApproved.setStatus(BookingStatus.APPROVED);

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(activeFacility));
        when(bookingRepository.findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of(existingApproved));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_throwsNotFound_whenFacilityDoesNotExist() {
        BookingRequest request = new BookingRequest(
                999L,
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 12, 0),
                "resident-001",
                "RESIDENT"
        );

        when(facilityRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(request)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void updateBookingStatus_throwsBadRequest_whenBookingIsNotPending() {
        Booking alreadyApproved = new Booking();
        alreadyApproved.setId(1L);
        alreadyApproved.setStatus(BookingStatus.APPROVED);
        alreadyApproved.setFacility(activeFacility);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(alreadyApproved));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.updateBookingStatus(1L, BookingStatus.APPROVED)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}