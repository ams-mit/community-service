package lk.ac.kln.apartment.community_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.ac.kln.apartment.community_service.entity.FacilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String location;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
    private FacilityStatus status;
    private String imageUrl;
    private String rulesAndGuidelines;

    @NotNull(message = "Booking fee is required")
    @Min(value = 0, message = "Booking fee cannot be negative")
    private Double bookingFee;

    @NotNull(message = "Max hours per booking is required")
    @Min(value = 1, message = "Max hours per booking must be at least 1")
    private Integer maxHoursPerBooking;
}
