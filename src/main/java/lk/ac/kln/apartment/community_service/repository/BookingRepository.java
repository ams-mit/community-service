package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByFacilityIdAndStatus(Long facilityId, BookingStatus status);

    List<Booking> findByFacilityIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long facilityId,
            BookingStatus status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}