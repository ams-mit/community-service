package kln.ams.community.repository;

import kln.ams.community.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, String> {

    boolean existsByAnnouncementIdAndUserId(String announcementId, String userId);

    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(String announcementId, String userId);

    long countByAnnouncementId(String announcementId);
}
