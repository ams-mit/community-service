package kln.ams.community.service;

import kln.ams.community.dto.*;
import kln.ams.community.entity.*;
import kln.ams.community.exception.BusinessRuleViolationException;
import kln.ams.community.exception.ForbiddenException;
import kln.ams.community.exception.InvalidStatusTransitionException;
import kln.ams.community.exception.ResourceNotFoundException;
import kln.ams.community.repository.AnnouncementAudienceRepository;
import kln.ams.community.repository.AnnouncementReadRepository;
import kln.ams.community.repository.AnnouncementRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAudienceRepository audienceRepository;
    private final AnnouncementReadRepository readRepository;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            AnnouncementAudienceRepository audienceRepository,
            AnnouncementReadRepository readRepository) {
        this.announcementRepository = announcementRepository;
        this.audienceRepository = audienceRepository;
        this.readRepository = readRepository;
    }

    public AnnouncementResponse createAnnouncement(AnnouncementCreateRequest request) {
        SecurityUtils.requireRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();

        if (request.getExpiresAt() != null && request.getScheduledAt() != null) {
            if (request.getExpiresAt().isBefore(request.getScheduledAt())) {
                throw new BusinessRuleViolationException("expiresAt must be after scheduledAt", "VALIDATION_ERROR");
            }
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory() != null ? request.getCategory() : "GENERAL")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .status(AnnouncementStatus.DRAFT)
                .audienceType(request.getAudienceType() != null ? request.getAudienceType() : AudienceType.ALL_RESIDENTS)
                .scheduledAt(request.getScheduledAt())
                .expiresAt(request.getExpiresAt())
                .createdByUserId(principal.getUserId())
                .build();

        announcement = announcementRepository.save(announcement);

        saveAudiences(announcement.getId(), announcement.getAudienceType(), request.getRoleCodes(), request.getUnitIds());

        List<String> roleCodes = request.getRoleCodes() != null ? request.getRoleCodes() : List.of();
        List<String> unitIds = request.getUnitIds() != null ? request.getUnitIds() : List.of();

        return AnnouncementResponse.fromEntity(announcement, roleCodes, unitIds, false);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementResponse> listAnnouncements(
            AnnouncementStatus status,
            String category,
            String priority,
            AudienceType audienceType,
            LocalDate publishedFrom,
            LocalDate publishedTo,
            int page,
            int size) {

        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        boolean isStaff = principal.hasAnyRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);

        Specification<Announcement> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (!isStaff) {
                // Regular users can only see PUBLISHED announcements
                predicates = cb.and(predicates, cb.equal(root.get("status"), AnnouncementStatus.PUBLISHED));
                // And non-expired
                var expNull = cb.isNull(root.get("expiresAt"));
                var expFuture = cb.greaterThan(root.get("expiresAt"), Instant.now());
                predicates = cb.and(predicates, cb.or(expNull, expFuture));
            } else if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (category != null && !category.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }
            if (priority != null && !priority.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("priority"), priority));
            }
            if (audienceType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("audienceType"), audienceType));
            }
            if (publishedFrom != null) {
                Instant fromInstant = publishedFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("publishedAt"), fromInstant));
            }
            if (publishedTo != null) {
                Instant toInstant = publishedTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates = cb.and(predicates, cb.lessThan(root.get("publishedAt"), toInstant));
            }

            return predicates;
        };

        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Announcement> announcements = announcementRepository.findAll(spec, pageable);

        return announcements.map(a -> {
            List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
            List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
            List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());
            boolean isRead = readRepository.existsByAnnouncementIdAndUserId(a.getId(), principal.getUserId());
            return AnnouncementResponse.fromEntity(a, roles, units, isRead);
        });
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse getAnnouncementById(String announcementId) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        Announcement a = findAnnouncementOrThrow(announcementId);

        boolean isStaff = principal.hasAnyRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        if (!isStaff && a.getStatus() != AnnouncementStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Announcement not found", "ANNOUNCEMENT_NOT_FOUND");
        }

        List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
        List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
        List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());
        boolean isRead = readRepository.existsByAnnouncementIdAndUserId(a.getId(), principal.getUserId());

        return AnnouncementResponse.fromEntity(a, roles, units, isRead);
    }

    public AnnouncementResponse updateAnnouncement(String announcementId, AnnouncementUpdateRequest request) {
        SecurityUtils.requireRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Announcement a = findAnnouncementOrThrow(announcementId);

        if (a.getStatus() == AnnouncementStatus.CANCELLED || a.getStatus() == AnnouncementStatus.EXPIRED) {
            throw new InvalidStatusTransitionException(
                    "Cannot update announcement in " + a.getStatus() + " status",
                    "ANNOUNCEMENT_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            a.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            a.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            a.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            a.setPriority(request.getPriority());
        }
        if (request.getAudienceType() != null) {
            a.setAudienceType(request.getAudienceType());
        }
        if (request.getScheduledAt() != null) {
            a.setScheduledAt(request.getScheduledAt());
        }
        if (request.getExpiresAt() != null) {
            a.setExpiresAt(request.getExpiresAt());
        }

        a = announcementRepository.save(a);

        if (request.getRoleCodes() != null || request.getUnitIds() != null) {
            audienceRepository.deleteByAnnouncementId(a.getId());
            saveAudiences(a.getId(), a.getAudienceType(), request.getRoleCodes(), request.getUnitIds());
        }

        List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
        List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
        List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());

        return AnnouncementResponse.fromEntity(a, roles, units, false);
    }

    public AnnouncementResponse publishAnnouncement(String announcementId) {
        SecurityUtils.requireRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Announcement a = findAnnouncementOrThrow(announcementId);

        if (a.getStatus() == AnnouncementStatus.CANCELLED || a.getStatus() == AnnouncementStatus.EXPIRED) {
            throw new InvalidStatusTransitionException(
                    "Cannot publish announcement in " + a.getStatus() + " status",
                    "ANNOUNCEMENT_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        a.setStatus(AnnouncementStatus.PUBLISHED);
        a.setPublishedAt(Instant.now());
        a = announcementRepository.save(a);

        List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
        List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
        List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());

        return AnnouncementResponse.fromEntity(a, roles, units, false);
    }

    public AnnouncementResponse cancelAnnouncement(String announcementId, String reason) {
        SecurityUtils.requireRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("Reason is mandatory for announcement cancellation", "VALIDATION_ERROR");
        }

        Announcement a = findAnnouncementOrThrow(announcementId);
        a.setStatus(AnnouncementStatus.CANCELLED);
        a.setCancelReason(reason);
        a = announcementRepository.save(a);

        List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
        List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
        List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());

        return AnnouncementResponse.fromEntity(a, roles, units, false);
    }

    public AnnouncementReadResponse recordRead(String announcementId) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        Announcement a = findAnnouncementOrThrow(announcementId);

        if (!readRepository.existsByAnnouncementIdAndUserId(a.getId(), principal.getUserId())) {
            AnnouncementRead ar = AnnouncementRead.builder()
                    .announcementId(a.getId())
                    .userId(principal.getUserId())
                    .readAt(Instant.now())
                    .build();
            readRepository.save(ar);
        }

        return AnnouncementReadResponse.builder()
                .announcementId(a.getId())
                .userId(principal.getUserId())
                .readAt(Instant.now())
                .build();
    }

    @Transactional(readOnly = true)
    public AudiencePreviewResponse getAudiencePreview(String announcementId) {
        SecurityUtils.requireRole(RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Announcement a = findAnnouncementOrThrow(announcementId);

        List<AnnouncementAudience> auds = audienceRepository.findByAnnouncementId(a.getId());
        List<String> roles = auds.stream().map(AnnouncementAudience::getRoleCode).filter(r -> r != null).collect(Collectors.toList());
        List<String> units = auds.stream().map(AnnouncementAudience::getUnitId).filter(u -> u != null).collect(Collectors.toList());

        long estimatedCount;
        switch (a.getAudienceType()) {
            case ROLE -> estimatedCount = roles.size() > 0 ? (roles.size() * 10L) : 0;
            case UNIT -> estimatedCount = units.size() > 0 ? (units.size() * 2L) : 0;
            case ALL_RESIDENTS -> estimatedCount = 50;
            default -> estimatedCount = 10;
        }

        return AudiencePreviewResponse.builder()
                .announcementId(a.getId())
                .audienceType(a.getAudienceType())
                .eligibleRecipientCount(estimatedCount)
                .roleCodes(roles)
                .unitIds(units)
                .build();
    }

    private Announcement findAnnouncementOrThrow(String announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId, "ANNOUNCEMENT_NOT_FOUND"));
    }

    private void saveAudiences(String announcementId, AudienceType audienceType, List<String> roleCodes, List<String> unitIds) {
        List<AnnouncementAudience> audiences = new ArrayList<>();
        if (roleCodes != null) {
            for (String r : roleCodes) {
                audiences.add(AnnouncementAudience.builder()
                        .announcementId(announcementId)
                        .audienceType(audienceType.name())
                        .roleCode(r)
                        .build());
            }
        }
        if (unitIds != null) {
            for (String u : unitIds) {
                audiences.add(AnnouncementAudience.builder()
                        .announcementId(announcementId)
                        .audienceType(audienceType.name())
                        .unitId(u)
                        .build());
            }
        }
        if (!audiences.isEmpty()) {
            audienceRepository.saveAll(audiences);
        }
    }
}
