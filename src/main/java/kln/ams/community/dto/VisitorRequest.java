package kln.ams.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRequest {

    @NotBlank(message = "unitId is required")
    private String unitId;

    @NotBlank(message = "visitorName is required")
    private String visitorName;

    private String visitorContact;

    @NotNull(message = "visitDate is required")
    private LocalDate visitDate;

    private String expectedArrival;

    private String expectedDeparture;

    @NotBlank(message = "purpose is required")
    private String purpose;

    private String notes;
}
