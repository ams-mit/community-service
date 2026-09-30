package kln.ams.community.repository;

import kln.ams.community.entity.Notification;
import kln.ams.community.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String>, JpaSpecificationExecutor<Notification> {

    List<Notification> findByRecipientUserIdAndStatus(String recipientUserId, NotificationStatus status);

    long countByRecipientUserIdAndStatus(String recipientUserId, NotificationStatus status);

    long countByRecipientUserId(String recipientUserId);

    Optional<Notification> findFirstBySourceServiceAndSourceEntityTypeAndSourceEntityIdAndTypeAndRecipientUserId(
            String sourceService,
            String sourceEntityType,
            String sourceEntityId,
            String type,
            String recipientUserId
    );
}
