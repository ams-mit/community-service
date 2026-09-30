package kln.ams.community.repository;

import kln.ams.community.entity.AnnouncementAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementAudienceRepository extends JpaRepository<AnnouncementAudience, String> {

    List<AnnouncementAudience> findByAnnouncementId(String announcementId);

    void deleteByAnnouncementId(String announcementId);
}
