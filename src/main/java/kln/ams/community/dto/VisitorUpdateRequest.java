package kln.ams.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorUpdateRequest {

    private String visitorName;
    private String visitorContact;
    private LocalDate visitDate;
    private String expectedArrival;
    private String expectedDeparture;
    private String purpose;
    private String notes;
}
