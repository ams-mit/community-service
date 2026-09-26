package lk.ac.kln.apartment.community_service.service;

import jakarta.annotation.PostConstruct;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import lk.ac.kln.apartment.community_service.dto.NotificationResponse;
import lk.ac.kln.apartment.community_service.entity.Notification;
import lk.ac.kln.apartment.community_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void initSampleNotificationsIfEmpty() {
        try {
            if (notificationRepository.count() == 0) {
                log.info("Seeding initial community workflow notifications...");

                Notification n1 = new Notification();
                n1.setType("maintenance");
                n1.setCategory("MAINTENANCE");
                n1.setPriority("high");
                n1.setTitle("Quarterly Fire Alarm & Siren Verification");
                n1.setSummary("Testing of sirens, detectors, and emergency elevators this Saturday at 10:00 AM.");
                n1.setMessage("The annual quarterly fire safety inspection, smoke detector diagnostic, and emergency siren verification will take place this Saturday from 10:00 AM to 11:30 AM across all residential towers and common corridors. Building elevators will briefly park at ground level for 10 minutes during the cycle. No resident evacuation is required.");
                n1.setIssuedBy("Central Operations & Safety Division");
                n1.setAffectedArea("Tower A, Tower B & Underground Parking");
                n1.setActionRoute("/announcements");
                n1.setActionLabel("View Bulletin");
                n1.setRecipientRole("ALL");
                notificationRepository.save(n1);

                Notification n2 = new Notification();
                n2.setType("facility");
                n2.setCategory("FACILITY");
                n2.setPriority("normal");
                n2.setTitle("Olympic Swimming Pool Scheduled Maintenance");
                n2.setSummary("Water purification and filtration system maintenance scheduled Monday morning.");
                n2.setMessage("The East Wing Olympic Swimming Pool & Sundeck will undergo routine water chemical balancing, filter backwash, and sanitization on Monday morning between 06:00 AM and 12:00 PM. Reservations will resume at 12:30 PM. All other residential amenities remain open.");
                n2.setIssuedBy("Aquatics & Recreation Management");
                n2.setAffectedArea("East Wing Courtyard Pool & Sundeck");
                n2.setActionRoute("/facilities");
                n2.setActionLabel("Check Facilities");
                n2.setRecipientRole("ALL");
                notificationRepository.save(n2);

                Notification n3 = new Notification();
                n3.setType("security");
                n3.setCategory("SECURITY");
                n3.setPriority("normal");
                n3.setTitle("Digital Gate Entry Pass System Active");
                n3.setSummary("Digital QR passes for pre-registered guests are live at Gate 1 and Tower checkpoints.");
                n3.setMessage("All security gate checkpoints now feature automated vector QR code scanners. When you pre-register a visitor in the Resident Portal, a scannable digital gate entry pass is automatically generated for instant arrival clearance.");
                n3.setIssuedBy("Resident Security & Access Control");
                n3.setAffectedArea("Main Security Gate 1 & Tower Entrances");
                n3.setActionRoute("/visitors");
                n3.setActionLabel("Manage Guest Passes");
                n3.setRecipientRole("ALL");
                notificationRepository.save(n3);

                Notification n4 = new Notification();
                n4.setType("facility");
                n4.setCategory("FACILITY");
                n4.setPriority("normal");
                n4.setTitle("Facility Booking Status: Active Review");
                n4.setSummary("Your amenity reservation requests are monitored by management.");
                n4.setMessage("You have active reservations in the community facilities ledger. Slots are held according to apartment bylaws. Please check your reservation ledger for verification status and operating hours.");
                n4.setIssuedBy("Facility Administration");
                n4.setAffectedArea("Clubhouse & Sports Complex");
                n4.setActionRoute("/facilities");
                n4.setActionLabel("View Reservations");
                n4.setRecipientRole("ALL");
                notificationRepository.save(n4);

                log.info("Initialized 4 sample community workflow notifications successfully.");
            }
        } catch (Exception e) {
            log.warn("Could not seed initial notifications (will proceed): {}", e.getMessage());
        }
    }

    public List<NotificationResponse> getNotifications(String recipientId, String role, Boolean unreadOnly) {
        return notificationRepository.findFiltered(recipientId, role, unreadOnly)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientId(request.getRecipientId());
        notification.setRecipientRole(request.getRecipientRole());
        notification.setTitle(request.getTitle());
        notification.setSummary(request.getSummary());
        notification.setMessage(request.getMessage() != null ? request.getMessage() : request.getSummary());
        notification.setType(request.getType() != null ? request.getType().toLowerCase() : "general");
        notification.setCategory(request.getCategory() != null ? request.getCategory().toUpperCase() : "GENERAL");
        notification.setPriority(request.getPriority() != null ? request.getPriority().toLowerCase() : "normal");
        notification.setIssuedBy(request.getIssuedBy() != null ? request.getIssuedBy() : "Community Administration");
        notification.setAffectedArea(request.getAffectedArea());
        notification.setActionRoute(request.getActionRoute());
        notification.setActionLabel(request.getActionLabel());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    @Transactional
    public int markAllAsRead(String recipientId) {
        return notificationRepository.markAllAsRead(recipientId);
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    public NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getRecipientRole(),
                notification.getTitle(),
                notification.getSummary(),
                notification.getMessage(),
                notification.getType(),
                notification.getCategory(),
                notification.getPriority(),
                notification.getIssuedBy(),
                notification.getAffectedArea(),
                notification.getActionRoute(),
                notification.getActionLabel(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
