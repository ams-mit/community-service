package kln.ams.community.repository;

import kln.ams.community.entity.Visitor;
import kln.ams.community.entity.VisitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, String>, JpaSpecificationExecutor<Visitor> {

    boolean existsByUnitIdAndVisitorNameIgnoreCaseAndVisitDateAndStatusIn(
            String unitId,
            String visitorName,
            LocalDate visitDate,
            Collection<VisitorStatus> statuses
    );

    List<Visitor> findByResidentUserId(String residentUserId);

    List<Visitor> findByUnitId(String unitId);
}
