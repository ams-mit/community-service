package kln.ams.community.repository;

import kln.ams.community.entity.VisitorHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorHistoryRepository extends JpaRepository<VisitorHistory, String> {

    List<VisitorHistory> findByVisitorIdOrderByChangedAtDesc(String visitorId);
}
