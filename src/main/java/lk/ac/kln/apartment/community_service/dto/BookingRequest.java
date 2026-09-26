package lk.ac.kln.apartment.community_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private Long facilityId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Requester info
    private String requesterId;
    private String requesterRole;
    private String requesterName;
    private String unitId;

    // Additional booking details
    private String purpose;
    private Integer attendeeCount;

    // Staff booking on behalf
    private String bookedByStaffId;
    private String bookedByStaffName;

    public BookingRequest(Long facilityId, LocalDateTime startTime, LocalDateTime endTime, String requesterId, String requesterRole) {
        this.facilityId = facilityId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requesterId = requesterId;
        this.requesterRole = requesterRole;
    }
}