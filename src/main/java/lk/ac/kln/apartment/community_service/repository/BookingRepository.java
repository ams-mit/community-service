package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Booking;
import lk.ac.kln.apartment.community_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT b FROM Booking b WHERE " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:facilityId IS NULL OR b.facility.id = :facilityId) AND " +
            "(:requesterId IS NULL OR b.requesterId = :requesterId) " +
            "ORDER BY b.startTime DESC")
    List<Booking> findFiltered(
            @Param("status") BookingStatus status,
            @Param("facilityId") Long facilityId,
            @Param("requesterId") String requesterId
    );
}