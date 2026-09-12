package lk.ac.kln.apartment.community_service.controller;

import lk.ac.kln.apartment.community_service.dto.FacilityResponse;
import lk.ac.kln.apartment.community_service.service.FacilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}