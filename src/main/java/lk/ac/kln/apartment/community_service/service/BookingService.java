package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.BookingRequest;
import lk.ac.kln.apartment.community_service.dto.BookingResponse;
import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lk.ac.kln.apartment.community_service.entity.Facility;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lk.ac.kln.apartment.community_service.repository.BookingRepository;
import lk.ac.kln.apartment.community_service.repository.FacilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;

    public BookingService(BookingRepository bookingRepository, FacilityRepository facilityRepository) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
    }

    public BookingResponse createBooking(BookingRequest request) {

        // Rule 1: facility must exist
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Facility not found"));

        // Rule 2: facility must be active
        if (facility.getStatus() != FacilityStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Facility is not active");
        }

        // Rule 3: end time must be after start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        // Rule 4: no overlapping APPROVED booking for this facility
        List<Booking> overlapping = bookingRepository
                .findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        facility.getId(),
                        BookingStatus.APPROVED,
                        request.getEndTime(),
                        request.getStartTime()
                );

        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This time slot conflicts with an approved booking");
        }

        // All checks passed — create the booking
        Booking booking = new Booking();
        booking.setFacility(facility);
        booking.setRequesterId(request.getRequesterId());
        booking.setRequesterRole(request.getRequesterRole());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getFacility().getId(),
                booking.getFacility().getName(),
                booking.getRequesterId(),
                booking.getRequesterRole(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}