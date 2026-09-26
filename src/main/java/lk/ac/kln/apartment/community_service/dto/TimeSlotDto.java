package lk.ac.kln.apartment.community_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private Long bookingId;
    private String purpose;
}
