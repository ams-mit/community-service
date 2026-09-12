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

    // Temporary fields — these will normally come from the verified JWT,
    // not from the request body. We use them here only until the
    // security filter is built. Remove once JWT parsing is added.
    private String requesterId;
    private String requesterRole;
}