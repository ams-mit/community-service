package lk.ac.kln.apartment.community_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRequest {

    private String visitorName;
    private String residentId;
    private String unitId;
    private String purpose;
    private LocalDate visitDate;
}