package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Announcement;
import lk.ac.kln.apartment.community_service.entity.AnnouncementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByTargetRoleInOrTargetRole(List<String> roles, String allValue);

    List<Announcement> findByStatusOrderByCreatedAtDesc(AnnouncementStatus status);

    @Query("SELECT a FROM Announcement a WHERE a.status = :status AND " +
            "(UPPER(a.targetRole) = UPPER(:role) OR UPPER(a.targetRole) = 'ALL') " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findActiveByRole(
            @Param("role") String role,
            @Param("status") AnnouncementStatus status
    );

    List<Announcement> findAllByOrderByCreatedAtDesc();
}