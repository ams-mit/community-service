package lk.ac.kln.apartment.community_service.dto;

import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long facilityId;
    private String facilityName;
    private String requesterId;
    private String requesterRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
}