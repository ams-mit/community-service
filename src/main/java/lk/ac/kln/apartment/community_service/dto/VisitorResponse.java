package lk.ac.kln.apartment.community_service.dto;

import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorResponse {

    private Long id;
    private String visitorName;
    private String residentId;
    private String unitId;
    private String purpose;
    private LocalDate visitDate;
    private String visitorPhone;
    private String vehicleNumber;
    private String passCode;
    private VisitorStatus status;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime createdAt;
}