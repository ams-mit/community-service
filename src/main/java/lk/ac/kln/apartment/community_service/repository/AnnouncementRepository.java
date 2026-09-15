package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByTargetRoleInOrTargetRole(List<String> roles, String allValue);
}