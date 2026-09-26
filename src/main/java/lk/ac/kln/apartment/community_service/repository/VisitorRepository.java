package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Visitor;
import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    Optional<Visitor> findByPassCode(String passCode);

    List<Visitor> findByStatusAndVisitDateBefore(VisitorStatus status, LocalDate visitDate);

    @Query("SELECT v FROM Visitor v WHERE " +
            "(:residentId IS NULL OR v.residentId = :residentId) AND " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:visitDate IS NULL OR v.visitDate = :visitDate) " +
            "ORDER BY v.createdAt DESC")
    List<Visitor> findFiltered(
            @Param("residentId") String residentId,
            @Param("status") VisitorStatus status,
            @Param("visitDate") LocalDate visitDate
    );
}