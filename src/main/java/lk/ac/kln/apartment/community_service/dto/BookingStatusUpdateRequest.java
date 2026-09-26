package lk.ac.kln.apartment.community_service.dto;

import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateRequest {

    private BookingStatus status;
    private String rejectionReason;
}