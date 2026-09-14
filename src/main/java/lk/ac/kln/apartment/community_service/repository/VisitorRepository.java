package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
}