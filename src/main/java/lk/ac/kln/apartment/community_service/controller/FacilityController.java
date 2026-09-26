package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.FacilityAvailabilityResponse;
import lk.ac.kln.apartment.community_service.dto.FacilityRequest;
import lk.ac.kln.apartment.community_service.dto.FacilityResponse;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lk.ac.kln.apartment.community_service.service.FacilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public List<FacilityResponse> getAllFacilities() {
        return facilityService.getAllFacilities();
    }

    @GetMapping("/{id}")
    public FacilityResponse getFacilityById(@PathVariable Long id) {
        return facilityService.getFacilityById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityResponse createFacility(@RequestBody FacilityRequest request) {
        return facilityService.createFacility(request);
    }

    @PutMapping("/{id}")
    public FacilityResponse updateFacility(@PathVariable Long id, @RequestBody FacilityRequest request) {
        return facilityService.updateFacility(id, request);
    }

    @PatchMapping("/{id}/status")
    public FacilityResponse toggleStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> statusBody) {
        FacilityStatus status = null;
        if (statusBody != null && statusBody.containsKey("status")) {
            status = FacilityStatus.valueOf(statusBody.get("status").toUpperCase());
        }
        return facilityService.toggleFacilityStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    public FacilityAvailabilityResponse getAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return facilityService.getFacilityAvailability(id, queryDate);
    }
}