package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.BookingRequest;
import lk.ac.kln.apartment.community_service.dto.BookingResponse;
import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lk.ac.kln.apartment.community_service.entity.Facility;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lk.ac.kln.apartment.community_service.repository.BookingRepository;
import lk.ac.kln.apartment.community_service.repository.FacilityRepository;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FacilityRepository facilityRepository;
    private final NotificationService notificationService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, FacilityRepository facilityRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.facilityRepository = facilityRepository;
        this.notificationService = notificationService;
    }

    public BookingService(BookingRepository bookingRepository, FacilityRepository facilityRepository) {
        this(bookingRepository, facilityRepository, null);
    }

    public List<BookingResponse> getBookings(BookingStatus status, Long facilityId, String requesterId) {
        return bookingRepository.findFiltered(status, facilityId, requesterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found with id: " + bookingId));
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        // Rule 1: facility must exist
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Facility not found"));

        // Rule 2: facility must be active
        if (facility.getStatus() != FacilityStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Facility is not active or under maintenance");
        }

        // Rule 3: end time must be after start time
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        // Rule 4: capacity check
        if (facility.getCapacity() != null && request.getAttendeeCount() != null && request.getAttendeeCount() > facility.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendee count (" + request.getAttendeeCount() + ") exceeds facility capacity (" + facility.getCapacity() + ")");
        }

        // Rule 5: no overlapping APPROVED booking for this facility
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
        booking.setRequesterName(request.getRequesterName());
        booking.setUnitId(request.getUnitId());
        booking.setPurpose(request.getPurpose());
        booking.setAttendeeCount(request.getAttendeeCount());
        booking.setBookedByStaffId(request.getBookedByStaffId());
        booking.setBookedByStaffName(request.getBookedByStaffName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        return updateBookingStatus(bookingId, newStatus, null);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus, String rejectionReason) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (newStatus == BookingStatus.CANCELLED) {
            return cancelBooking(bookingId);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only PENDING bookings can have their status updated (current status: " + booking.getStatus() + ")");
        }

        if (newStatus == BookingStatus.APPROVED) {
            // Check for approved conflicts
            List<Booking> overlapping = bookingRepository
                    .findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                            booking.getFacility().getId(),
                            BookingStatus.APPROVED,
                            booking.getEndTime(),
                            booking.getStartTime()
                    );

            boolean hasConflict = overlapping.stream().anyMatch(b -> !b.getId().equals(booking.getId()));
            if (hasConflict) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot approve — this time slot now conflicts with another approved booking");
            }

            // Auto-reject conflicting PENDING bookings
            List<Booking> conflictingPending = bookingRepository
                    .findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                            booking.getFacility().getId(),
                            BookingStatus.PENDING,
                            booking.getEndTime(),
                            booking.getStartTime()
                    );

            for (Booking pending : conflictingPending) {
                if (!pending.getId().equals(booking.getId())) {
                    pending.setStatus(BookingStatus.REJECTED);
                    pending.setRejectionReason("Slot reserved by another approved booking (#" + booking.getId() + ")");
                    bookingRepository.save(pending);
                }
            }
        } else if (newStatus == BookingStatus.REJECTED) {
            if (rejectionReason != null && !rejectionReason.isBlank()) {
                booking.setRejectionReason(rejectionReason);
            }
        }

        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);

        if (notificationService != null && (newStatus == BookingStatus.APPROVED || newStatus == BookingStatus.REJECTED)) {
            try {
                NotificationRequest notif = new NotificationRequest();
                notif.setRecipientId(saved.getRequesterId());
                notif.setRecipientRole(saved.getRequesterRole());
                notif.setTitle("Facility Reservation " + saved.getStatus());
                notif.setSummary("Reservation for " + saved.getFacility().getName() + " is now " + saved.getStatus() + ".");
                if (saved.getStatus() == BookingStatus.REJECTED && saved.getRejectionReason() != null) {
                    notif.setMessage("Rejection reason: " + saved.getRejectionReason());
                } else {
                    notif.setMessage(notif.getSummary());
                }
                notif.setType("facility");
                notif.setCategory("FACILITY");
                notif.setPriority(saved.getStatus() == BookingStatus.REJECTED ? "high" : "normal");
                notif.setIssuedBy("Facility Administration");
                notif.setActionRoute("/facilities");
                notif.setActionLabel("View Reservations");
                notificationService.createNotification(notif);
            } catch (Exception ignored) {
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a rejected booking");
        }

        // Check if booking is in the future
        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot cancel a past or current reservation");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        if (notificationService != null) {
            try {
                NotificationRequest notif = new NotificationRequest();
                notif.setRecipientId(saved.getRequesterId());
                notif.setRecipientRole(saved.getRequesterRole());
                notif.setTitle("Facility Reservation Cancelled");
                notif.setSummary("Reservation for " + saved.getFacility().getName() + " on " + saved.getStartTime().toLocalDate() + " has been cancelled.");
                notif.setMessage(notif.getSummary());
                notif.setType("facility");
                notif.setCategory("FACILITY");
                notif.setPriority("normal");
                notif.setIssuedBy("Facility Administration");
                notif.setActionRoute("/facilities");
                notif.setActionLabel("View Reservations");
                notificationService.createNotification(notif);
            } catch (Exception ignored) {
            }
        }

        return mapToResponse(saved);
    }

    public BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getFacility().getId(),
                booking.getFacility().getName(),
                booking.getRequesterId(),
                booking.getRequesterRole(),
                booking.getRequesterName(),
                booking.getUnitId(),
                booking.getPurpose(),
                booking.getAttendeeCount(),
                booking.getRejectionReason(),
                booking.getBookedByStaffId(),
                booking.getBookedByStaffName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}