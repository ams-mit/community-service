package lk.ac.kln.apartment.community_service.dto;

import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityRequest {

    private String name;
    private String type;
    private Integer capacity;
    private String location;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
    private FacilityStatus status;
    private String imageUrl;
    private String rulesAndGuidelines;
    private Double bookingFee;
    private Integer maxHoursPerBooking;
}
