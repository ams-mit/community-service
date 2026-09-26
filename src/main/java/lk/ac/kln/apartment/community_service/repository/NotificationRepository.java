package lk.ac.kln.apartment.community_service.repository;

import lk.ac.kln.apartment.community_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE " +
            "(:recipientId IS NULL OR n.recipientId IS NULL OR n.recipientId = 'ALL' OR n.recipientId = :recipientId) AND " +
            "(:role IS NULL OR n.recipientRole IS NULL OR n.recipientRole = 'ALL' OR UPPER(n.recipientRole) = UPPER(:role)) AND " +
            "(:unreadOnly IS NULL OR :unreadOnly = false OR n.isRead = false) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findFiltered(
            @Param("recipientId") String recipientId,
            @Param("role") String role,
            @Param("unreadOnly") Boolean unreadOnly
    );

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE " +
            "(:recipientId IS NULL OR n.recipientId = :recipientId OR n.recipientId IS NULL OR n.recipientId = 'ALL') AND " +
            "n.isRead = false")
    int markAllAsRead(@Param("recipientId") String recipientId);
}
