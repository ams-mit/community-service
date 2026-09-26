package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.FacilityAvailabilityResponse;
import lk.ac.kln.apartment.community_service.dto.FacilityRequest;
import lk.ac.kln.apartment.community_service.dto.FacilityResponse;
import lk.ac.kln.apartment.community_service.dto.TimeSlotDto;
import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lk.ac.kln.apartment.community_service.entity.Facility;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lk.ac.kln.apartment.community_service.repository.BookingRepository;
import lk.ac.kln.apartment.community_service.repository.FacilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final BookingRepository bookingRepository;

    public FacilityService(FacilityRepository facilityRepository, BookingRepository bookingRepository) {
        this.facilityRepository = facilityRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostConstruct
    public void initFacilitiesIfEmpty() {
        try {
            if (facilityRepository.count() == 0) {
                facilityRepository.save(new Facility(null, "Grand Clubhouse & Banquet Hall", "Event Space", 120, "Central Plaza, Ground Level", LocalTime.of(8, 0), LocalTime.of(23, 0), FacilityStatus.ACTIVE, null, "Quiet hours from 22:00. Advance booking required.", 50.0, 4));
                facilityRepository.save(new Facility(null, "Olympic Swimming Pool & Sundeck", "Recreation", 40, "East Wing Courtyard", LocalTime.of(6, 0), LocalTime.of(21, 0), FacilityStatus.ACTIVE, null, "Proper swimwear required at all times.", 0.0, 2));
                facilityRepository.save(new Facility(null, "Rooftop Tennis Court", "Sports", 4, "Tower A, Level 18", LocalTime.of(7, 0), LocalTime.of(20, 0), FacilityStatus.ACTIVE, null, "Non-marking tennis shoes only.", 15.0, 2));
                facilityRepository.save(new Facility(null, "Skyline Residents Lounge & Library", "Coworking & Study", 25, "Tower B, Level 15", LocalTime.of(7, 0), LocalTime.of(23, 0), FacilityStatus.ACTIVE, null, "Silent study and remote work zone.", 0.0, 6));
                facilityRepository.save(new Facility(null, "Fitness & Cardio Center", "Gymnasium", 30, "West Wing, Level 2", LocalTime.of(5, 30), LocalTime.of(22, 30), FacilityStatus.ACTIVE, null, "Sanitize equipment after use.", 0.0, 2));
                facilityRepository.save(new Facility(null, "Barbecue Pavilion & Garden", "Outdoor Dining", 20, "South Garden Lawn", LocalTime.of(11, 0), LocalTime.of(22, 0), FacilityStatus.ACTIVE, null, "Clean grills thoroughly after use.", 25.0, 3));
            }
        } catch (Exception ignored) {
        }
    }

    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FacilityResponse getFacilityById(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found with id: " + id));
        return mapToResponse(facility);
    }

    public FacilityResponse createFacility(FacilityRequest request) {
        Facility facility = new Facility();
        updateEntityFromRequest(facility, request);
        if (facility.getStatus() == null) {
            facility.setStatus(FacilityStatus.ACTIVE);
        }
        Facility saved = facilityRepository.save(facility);
        return mapToResponse(saved);
    }

    public FacilityResponse updateFacility(Long id, FacilityRequest request) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found with id: " + id));

        updateEntityFromRequest(facility, request);
        Facility updated = facilityRepository.save(facility);
        return mapToResponse(updated);
    }

    public FacilityResponse toggleFacilityStatus(Long id, FacilityStatus explicitStatus) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found with id: " + id));

        if (explicitStatus != null) {
            facility.setStatus(explicitStatus);
        } else {
            facility.setStatus(facility.getStatus() == FacilityStatus.ACTIVE ? FacilityStatus.INACTIVE : FacilityStatus.ACTIVE);
        }

        Facility updated = facilityRepository.save(facility);
        return mapToResponse(updated);
    }

    public void deleteFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found with id: " + id));

        // Soft-delete or deactivate
        facility.setStatus(FacilityStatus.INACTIVE);
        facilityRepository.save(facility);
    }

    public FacilityAvailabilityResponse getFacilityAvailability(Long id, LocalDate date) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found with id: " + id));

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<Booking> approvedBookings = bookingRepository
                .findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        facility.getId(),
                        BookingStatus.APPROVED,
                        dayEnd,
                        dayStart
                );

        LocalTime openTime = facility.getOperatingHoursStart() != null ? facility.getOperatingHoursStart() : LocalTime.of(6, 0);
        LocalTime closeTime = facility.getOperatingHoursEnd() != null ? facility.getOperatingHoursEnd() : LocalTime.of(22, 0);

        List<TimeSlotDto> bookedSlots = new ArrayList<>();
        for (Booking b : approvedBookings) {
            LocalTime slotStart = b.getStartTime().toLocalDate().isBefore(date) ? openTime : b.getStartTime().toLocalTime();
            LocalTime slotEnd = b.getEndTime().toLocalDate().isAfter(date) ? closeTime : b.getEndTime().toLocalTime();
            bookedSlots.add(new TimeSlotDto(slotStart, slotEnd, false, b.getId(), b.getPurpose()));
        }

        List<TimeSlotDto> availableSlots = new ArrayList<>();
        LocalTime current = openTime;
        while (current.isBefore(closeTime)) {
            LocalTime next = current.plusHours(1);
            if (next.isAfter(closeTime)) {
                next = closeTime;
            }

            LocalDateTime slotStartDateTime = date.atTime(current);
            LocalDateTime slotEndDateTime = date.atTime(next);

            boolean isOverlap = approvedBookings.stream().anyMatch(b ->
                    b.getStartTime().isBefore(slotEndDateTime) && b.getEndTime().isAfter(slotStartDateTime)
            );

            if (!isOverlap) {
                availableSlots.add(new TimeSlotDto(current, next, true, null, null));
            }

            current = next;
        }

        return new FacilityAvailabilityResponse(
                facility.getId(),
                facility.getName(),
                date,
                openTime,
                closeTime,
                bookedSlots,
                availableSlots
        );
    }

    private void updateEntityFromRequest(Facility facility, FacilityRequest request) {
        if (request.getName() != null) facility.setName(request.getName());
        if (request.getType() != null) facility.setType(request.getType());
        if (request.getCapacity() != null) facility.setCapacity(request.getCapacity());
        if (request.getLocation() != null) facility.setLocation(request.getLocation());
        if (request.getOperatingHoursStart() != null) facility.setOperatingHoursStart(request.getOperatingHoursStart());
        if (request.getOperatingHoursEnd() != null) facility.setOperatingHoursEnd(request.getOperatingHoursEnd());
        if (request.getStatus() != null) facility.setStatus(request.getStatus());
        if (request.getImageUrl() != null) facility.setImageUrl(request.getImageUrl());
        if (request.getRulesAndGuidelines() != null) facility.setRulesAndGuidelines(request.getRulesAndGuidelines());
        if (request.getBookingFee() != null) facility.setBookingFee(request.getBookingFee());
        if (request.getMaxHoursPerBooking() != null) facility.setMaxHoursPerBooking(request.getMaxHoursPerBooking());
    }

    public FacilityResponse mapToResponse(Facility facility) {
        return new FacilityResponse(
                facility.getId(),
                facility.getName(),
                facility.getType(),
                facility.getCapacity(),
                facility.getLocation(),
                facility.getOperatingHoursStart(),
                facility.getOperatingHoursEnd(),
                facility.getStatus(),
                facility.getImageUrl(),
                facility.getRulesAndGuidelines(),
                facility.getBookingFee(),
                facility.getMaxHoursPerBooking()
        );
    }
}