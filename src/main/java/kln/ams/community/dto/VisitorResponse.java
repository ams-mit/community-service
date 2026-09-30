package kln.ams.community.dto;

import kln.ams.community.entity.Visitor;
import kln.ams.community.entity.VisitorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorResponse {

    private String id;
    private String residentUserId;
    private String unitId;
    private String visitorName;
    private String visitorContact;
    private LocalDate visitDate;
    private String expectedArrival;
    private String expectedDeparture;
    private String purpose;
    private VisitorStatus status;
    private Instant checkInAt;
    private Instant checkOutAt;
    private String notes;
    private String createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static VisitorResponse fromEntity(Visitor v) {
        if (v == null) return null;
        return VisitorResponse.builder()
                .id(v.getId())
                .residentUserId(v.getResidentUserId())
                .unitId(v.getUnitId())
                .visitorName(v.getVisitorName())
                .visitorContact(v.getVisitorContact())
                .visitDate(v.getVisitDate())
                .expectedArrival(v.getExpectedArrival())
                .expectedDeparture(v.getExpectedDeparture())
                .purpose(v.getPurpose())
                .status(v.getStatus())
                .checkInAt(v.getCheckInAt())
                .checkOutAt(v.getCheckOutAt())
                .notes(v.getNotes())
                .createdByUserId(v.getCreatedByUserId())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
