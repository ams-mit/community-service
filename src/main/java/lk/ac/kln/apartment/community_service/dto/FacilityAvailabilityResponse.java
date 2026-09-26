package lk.ac.kln.apartment.community_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityAvailabilityResponse {

    private Long facilityId;
    private String facilityName;
    private LocalDate date;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
    private List<TimeSlotDto> bookedSlots;
    private List<TimeSlotDto> availableSlots;
}
