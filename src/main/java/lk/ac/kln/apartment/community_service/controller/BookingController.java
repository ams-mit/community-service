package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.BookingRequest;
import lk.ac.kln.apartment.community_service.dto.BookingResponse;
import lk.ac.kln.apartment.community_service.dto.BookingStatusUpdateRequest;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lk.ac.kln.apartment.community_service.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facilities/reservations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) String requesterId) {
        return bookingService.getBookings(status, facilityId, requesterId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PatchMapping("/{bookingId}/status")
    public BookingResponse updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody BookingStatusUpdateRequest request) {
        return bookingService.updateBookingStatus(bookingId, request.getStatus(), request.getRejectionReason());
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}