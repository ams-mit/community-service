package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.BookingRequest;
import lk.ac.kln.apartment.community_service.dto.BookingResponse;
import lk.ac.kln.apartment.community_service.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/facilities/reservations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }
}