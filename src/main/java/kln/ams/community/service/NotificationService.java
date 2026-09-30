package kln.ams.community.service;

import kln.ams.community.dto.*;
import kln.ams.community.entity.Notification;
import kln.ams.community.entity.NotificationStatus;
import kln.ams.community.exception.ForbiddenException;
import kln.ams.community.exception.ResourceNotFoundException;
import kln.ams.community.repository.NotificationRepository;
import kln.ams.community.security.JwtAuthPrincipal;
import kln.ams.community.security.RoleConstants;
import kln.ams.community.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse createInternalNotification(InternalNotificationRequest request) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        SecurityUtils.requireService(RoleConstants.ALLOWED_INTERNAL_SERVICES.toArray(new String[0]));

        String effectiveSourceService = (request.getSourceService() != null && !request.getSourceService().isBlank())
                ? request.getSourceService()
                : principal.getSubject();

        // Idempotency check
        if (request.getSourceEntityId() != null && !request.getSourceEntityId().isBlank()) {
            Optional<Notification> existing = notificationRepository
                    .findFirstBySourceServiceAndSourceEntityTypeAndSourceEntityIdAndTypeAndRecipientUserId(
                            effectiveSourceService,
                            request.getSourceEntityType(),
                            request.getSourceEntityId(),
                            request.getType(),
                            request.getRecipientUserId()
                    );
            if (existing.isPresent()) {
                return NotificationResponse.fromEntity(existing.get());
            }
        }

        Notification notification = Notification.builder()
                .recipientUserId(request.getRecipientUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .status(NotificationStatus.UNREAD)
                .deliveryStatus("DELIVERED")
                .sourceService(effectiveSourceService)
                .sourceEntityType(request.getSourceEntityType())
                .sourceEntityId(request.getSourceEntityId())
                .build();

        notification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(notification);
    }

    public NotificationResponse createInternalResidentNotification(InternalResidentNotificationRequest request) {
        InternalNotificationRequest req = InternalNotificationRequest.builder()
                .recipientUserId(request.getResidentUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .priority(request.getPriority())
                .sourceService(request.getSourceService())
                .sourceEntityType(request.getSourceEntityType())
                .sourceEntityId(request.getSourceEntityId())
                .build();
        return createInternalNotification(req);
    }

    public void createInternalNotificationDirect(
            String recipientUserId,
            String type,
            String title,
            String message,
            String priority,
            String sourceService,
            String sourceEntityType,
            String sourceEntityId) {

        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : "NORMAL")
                .status(NotificationStatus.UNREAD)
                .deliveryStatus("DELIVERED")
                .sourceService(sourceService)
                .sourceEntityType(sourceEntityType)
                .sourceEntityId(sourceEntityId)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public NotificationStatusResponse getInternalNotificationStatus(String notificationId) {
        SecurityUtils.requireService(RoleConstants.ALLOWED_INTERNAL_SERVICES.toArray(new String[0]));
        Notification notification = findNotificationOrThrow(notificationId);
        return NotificationStatusResponse.builder()
                .notificationId(notification.getId())
                .status(notification.getStatus())
                .deliveryStatus(notification.getDeliveryStatus())
                .readAt(notification.getReadAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMyNotifications(
            NotificationStatus status,
            String type,
            String priority,
            Boolean unreadOnly,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size) {

        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        String currentUserId = principal.getUserId();

        Specification<Notification> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("recipientUserId"), currentUserId));

            if (Boolean.TRUE.equals(unreadOnly)) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), NotificationStatus.UNREAD));
            } else if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (type != null && !type.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (priority != null && !priority.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("priority"), priority));
            }
            if (dateFrom != null) {
                Instant fromInstant = dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), fromInstant));
            }
            if (dateTo != null) {
                Instant toInstant = dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.lessThan(root.get("createdAt"), toInstant));
            }

            return predicates;
        };

        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationRepository.findAll(spec, pageable).map(NotificationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(String notificationId) {
        Notification notification = findNotificationOrThrow(notificationId);
        validateOwnership(notification);
        return NotificationResponse.fromEntity(notification);
    }

    public NotificationResponse markAsRead(String notificationId) {
        Notification notification = findNotificationOrThrow(notificationId);
        validateOwnership(notification);

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }
        return NotificationResponse.fromEntity(notification);
    }

    public NotificationReadAllResponse markAllAsRead() {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        String currentUserId = principal.getUserId();

        List<Notification> unreadList = notificationRepository.findByRecipientUserIdAndStatus(currentUserId, NotificationStatus.UNREAD);
        Instant now = Instant.now();
        for (Notification n : unreadList) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unreadList);

        return NotificationReadAllResponse.builder()
                .updatedCount(unreadList.size())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummary() {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        String currentUserId = principal.getUserId();

        long unread = notificationRepository.countByRecipientUserIdAndStatus(currentUserId, NotificationStatus.UNREAD);
        long total = notificationRepository.countByRecipientUserId(currentUserId);

        return NotificationSummaryResponse.builder()
                .unreadCount(unread)
                .totalCount(total)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getHistory(
            String type,
            String sourceService,
            String sourceEntityType,
            String priority,
            NotificationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size) {

        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        String currentUserId = principal.getUserId();

        Specification<Notification> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("recipientUserId"), currentUserId));

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (type != null && !type.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (sourceService != null && !sourceService.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("sourceService"), sourceService));
            }
            if (sourceEntityType != null && !sourceEntityType.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("sourceEntityType"), sourceEntityType));
            }
            if (priority != null && !priority.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("priority"), priority));
            }
            if (dateFrom != null) {
                Instant fromInstant = dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), fromInstant));
            }
            if (dateTo != null) {
                Instant toInstant = dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.lessThan(root.get("createdAt"), toInstant));
            }

            return predicates;
        };

        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationRepository.findAll(spec, pageable).map(NotificationResponse::fromEntity);
    }

    public NotificationResponse archiveNotification(String notificationId) {
        Notification notification = findNotificationOrThrow(notificationId);
        validateOwnership(notification);

        notification.setStatus(NotificationStatus.ARCHIVED);
        notification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(notification);
    }

    private Notification findNotificationOrThrow(String notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId, "NOTIFICATION_NOT_FOUND"));
    }

    private void validateOwnership(Notification notification) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.hasAnyRole(RoleConstants.SYSTEM_ADMINISTRATOR, RoleConstants.APARTMENT_MANAGER)) {
            return;
        }
        if (!notification.getRecipientUserId().equals(principal.getUserId())) {
            throw new ForbiddenException("Access denied to notification");
        }
    }
}
