package kln.ams.community.service;

import kln.ams.community.dto.VisitorRequest;
import kln.ams.community.dto.VisitorResponse;
import kln.ams.community.dto.VisitorUpdateRequest;
import kln.ams.community.entity.Visitor;
import kln.ams.community.entity.VisitorHistory;
import kln.ams.community.entity.VisitorStatus;
import kln.ams.community.exception.BusinessRuleViolationException;
import kln.ams.community.exception.DuplicateResourceException;
import kln.ams.community.exception.ForbiddenException;
import kln.ams.community.exception.InvalidStatusTransitionException;
import kln.ams.community.exception.ResourceNotFoundException;
import kln.ams.community.repository.VisitorHistoryRepository;
import kln.ams.community.repository.VisitorRepository;
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
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final VisitorHistoryRepository visitorHistoryRepository;
    private final NotificationService notificationService;

    public VisitorService(
            VisitorRepository visitorRepository,
            VisitorHistoryRepository visitorHistoryRepository,
            NotificationService notificationService) {
        this.visitorRepository = visitorRepository;
        this.visitorHistoryRepository = visitorHistoryRepository;
        this.notificationService = notificationService;
    }

    public VisitorResponse createVisitor(VisitorRequest request) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        SecurityUtils.requireRole(
                RoleConstants.TENANT_RESIDENT,
                RoleConstants.OWNER,
                RoleConstants.APARTMENT_MANAGER,
                RoleConstants.SECURITY_OFFICER,
                RoleConstants.SYSTEM_ADMINISTRATOR
        );

        if (request.getExpectedArrival() != null && request.getExpectedDeparture() != null) {
            if (request.getExpectedDeparture().compareTo(request.getExpectedArrival()) <= 0) {
                throw new BusinessRuleViolationException(
                        "expectedDeparture must be later than expectedArrival",
                        "VALIDATION_ERROR"
                );
            }
        }

        boolean duplicate = visitorRepository.existsByUnitIdAndVisitorNameIgnoreCaseAndVisitDateAndStatusIn(
                request.getUnitId(),
                request.getVisitorName(),
                request.getVisitDate(),
                Set.of(VisitorStatus.PENDING, VisitorStatus.APPROVED, VisitorStatus.CHECKED_IN)
        );
        if (duplicate) {
            throw new DuplicateResourceException(
                    "An active visitor request already exists for this visitor and unit on this date",
                    "DUPLICATE_VISITOR_REQUEST"
            );
        }

        String residentUserId = principal.getUserId();

        Visitor visitor = Visitor.builder()
                .residentUserId(residentUserId)
                .unitId(request.getUnitId())
                .visitorName(request.getVisitorName())
                .visitorContact(request.getVisitorContact())
                .visitDate(request.getVisitDate())
                .expectedArrival(request.getExpectedArrival())
                .expectedDeparture(request.getExpectedDeparture())
                .purpose(request.getPurpose())
                .status(VisitorStatus.PENDING)
                .notes(request.getNotes())
                .createdByUserId(principal.getUserId())
                .build();

        visitor = visitorRepository.save(visitor);

        recordHistory(visitor.getId(), "VISITOR_CREATED", null, VisitorStatus.PENDING, "Visitor request created", principal.getUserId());

        return VisitorResponse.fromEntity(visitor);
    }

    @Transactional(readOnly = true)
    public Page<VisitorResponse> listVisitors(
            String unitId,
            String residentUserId,
            VisitorStatus status,
            LocalDate visitDate,
            LocalDate dateFrom,
            LocalDate dateTo,
            String visitorName,
            int page,
            int size) {

        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        SecurityUtils.requireRole(
                RoleConstants.SYSTEM_ADMINISTRATOR,
                RoleConstants.APARTMENT_MANAGER,
                RoleConstants.SECURITY_OFFICER,
                RoleConstants.TENANT_RESIDENT,
                RoleConstants.OWNER
        );

        String effectiveResidentUserId = residentUserId;
        if (principal.hasAnyRole(RoleConstants.TENANT_RESIDENT, RoleConstants.OWNER)
                && !principal.hasAnyRole(RoleConstants.SYSTEM_ADMINISTRATOR, RoleConstants.APARTMENT_MANAGER, RoleConstants.SECURITY_OFFICER)) {
            effectiveResidentUserId = principal.getUserId();
        }

        final String finalResidentId = effectiveResidentUserId;

        Specification<Visitor> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (unitId != null && !unitId.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("unitId"), unitId));
            }
            if (finalResidentId != null && !finalResidentId.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("residentUserId"), finalResidentId));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (visitDate != null) {
                predicates = cb.and(predicates, cb.equal(root.get("visitDate"), visitDate));
            }
            if (dateFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("visitDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("visitDate"), dateTo));
            }
            if (visitorName != null && !visitorName.isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("visitorName")), "%" + visitorName.toLowerCase() + "%"));
            }

            return predicates;
        };

        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNumber = Math.max(page, 0);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return visitorRepository.findAll(spec, pageable).map(VisitorResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public VisitorResponse getVisitorById(String visitorId) {
        Visitor visitor = findVisitorOrThrow(visitorId);
        validateAccess(visitor);
        return VisitorResponse.fromEntity(visitor);
    }

    public VisitorResponse updateVisitor(String visitorId, VisitorUpdateRequest request) {
        Visitor visitor = findVisitorOrThrow(visitorId);
        validateAccess(visitor);

        if (visitor.getStatus() != VisitorStatus.PENDING && visitor.getStatus() != VisitorStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot update visitor in status: " + visitor.getStatus(),
                    "VISITOR_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        if (request.getVisitorName() != null && !request.getVisitorName().isBlank()) {
            visitor.setVisitorName(request.getVisitorName());
        }
        if (request.getVisitorContact() != null) {
            visitor.setVisitorContact(request.getVisitorContact());
        }
        if (request.getVisitDate() != null) {
            visitor.setVisitDate(request.getVisitDate());
        }
        if (request.getExpectedArrival() != null) {
            visitor.setExpectedArrival(request.getExpectedArrival());
        }
        if (request.getExpectedDeparture() != null) {
            visitor.setExpectedDeparture(request.getExpectedDeparture());
        }
        if (request.getPurpose() != null && !request.getPurpose().isBlank()) {
            visitor.setPurpose(request.getPurpose());
        }
        if (request.getNotes() != null) {
            visitor.setNotes(request.getNotes());
        }

        visitor = visitorRepository.save(visitor);
        recordHistory(visitor.getId(), "VISITOR_UPDATED", visitor.getStatus(), visitor.getStatus(), "Visitor details updated", SecurityUtils.requirePrincipal().getUserId());

        return VisitorResponse.fromEntity(visitor);
    }

    public VisitorResponse approveVisitor(String visitorId, String reason) {
        SecurityUtils.requireRole(RoleConstants.SECURITY_OFFICER, RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Visitor visitor = findVisitorOrThrow(visitorId);

        if (visitor.getStatus() != VisitorStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Only PENDING visitor requests can be approved. Current status: " + visitor.getStatus(),
                    "VISITOR_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        VisitorStatus prev = visitor.getStatus();
        visitor.setStatus(VisitorStatus.APPROVED);
        visitor = visitorRepository.save(visitor);

        String actor = SecurityUtils.requirePrincipal().getUserId();
        recordHistory(visitor.getId(), "VISITOR_APPROVED", prev, VisitorStatus.APPROVED, reason, actor);

        notificationService.createInternalNotificationDirect(
                visitor.getResidentUserId(),
                "VISITOR_APPROVED",
                "Visitor Request Approved",
                "Your visitor request for " + visitor.getVisitorName() + " has been approved.",
                "NORMAL",
                "community-service",
                "Visitor",
                visitor.getId()
        );

        return VisitorResponse.fromEntity(visitor);
    }

    public VisitorResponse rejectVisitor(String visitorId, String reason) {
        SecurityUtils.requireRole(RoleConstants.SECURITY_OFFICER, RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("Reason is mandatory for visitor rejection", "VALIDATION_ERROR");
        }

        Visitor visitor = findVisitorOrThrow(visitorId);
        if (visitor.getStatus() != VisitorStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Only PENDING visitor requests can be rejected. Current status: " + visitor.getStatus(),
                    "VISITOR_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        VisitorStatus prev = visitor.getStatus();
        visitor.setStatus(VisitorStatus.REJECTED);
        visitor = visitorRepository.save(visitor);

        String actor = SecurityUtils.requirePrincipal().getUserId();
        recordHistory(visitor.getId(), "VISITOR_REJECTED", prev, VisitorStatus.REJECTED, reason, actor);

        notificationService.createInternalNotificationDirect(
                visitor.getResidentUserId(),
                "VISITOR_REJECTED",
                "Visitor Request Rejected",
                "Your visitor request for " + visitor.getVisitorName() + " was rejected: " + reason,
                "HIGH",
                "community-service",
                "Visitor",
                visitor.getId()
        );

        return VisitorResponse.fromEntity(visitor);
    }

    public VisitorResponse checkInVisitor(String visitorId) {
        SecurityUtils.requireRole(RoleConstants.SECURITY_OFFICER, RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Visitor visitor = findVisitorOrThrow(visitorId);

        if (visitor.getStatus() != VisitorStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Visitor must be APPROVED before check-in. Current status: " + visitor.getStatus(),
                    "VISITOR_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        VisitorStatus prev = visitor.getStatus();
        visitor.setStatus(VisitorStatus.CHECKED_IN);
        visitor.setCheckInAt(Instant.now());
        visitor = visitorRepository.save(visitor);

        String actor = SecurityUtils.requirePrincipal().getUserId();
        recordHistory(visitor.getId(), "VISITOR_CHECKED_IN", prev, VisitorStatus.CHECKED_IN, "Visitor checked in", actor);

        notificationService.createInternalNotificationDirect(
                visitor.getResidentUserId(),
                "VISITOR_CHECKED_IN",
                "Visitor Checked In",
                visitor.getVisitorName() + " has checked in at the security desk.",
                "NORMAL",
                "community-service",
                "Visitor",
                visitor.getId()
        );

        return VisitorResponse.fromEntity(visitor);
    }

    public VisitorResponse checkOutVisitor(String visitorId) {
        SecurityUtils.requireRole(RoleConstants.SECURITY_OFFICER, RoleConstants.APARTMENT_MANAGER, RoleConstants.SYSTEM_ADMINISTRATOR);
        Visitor visitor = findVisitorOrThrow(visitorId);

        if (visitor.getStatus() != VisitorStatus.CHECKED_IN) {
            throw new InvalidStatusTransitionException(
                    "Visitor must be CHECKED_IN before check-out. Current status: " + visitor.getStatus(),
                    "VISITOR_STATUS_TRANSITION_NOT_ALLOWED"
            );
        }

        VisitorStatus prev = visitor.getStatus();
        visitor.setStatus(VisitorStatus.CHECKED_OUT);
        visitor.setCheckOutAt(Instant.now());
        visitor = visitorRepository.save(visitor);

        String actor = SecurityUtils.requirePrincipal().getUserId();
        recordHistory(visitor.getId(), "VISITOR_CHECKED_OUT", prev, VisitorStatus.CHECKED_OUT, "Visitor checked out", actor);

        return VisitorResponse.fromEntity(visitor);
    }

    private Visitor findVisitorOrThrow(String visitorId) {
        return visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found with id: " + visitorId, "VISITOR_NOT_FOUND"));
    }

    private void validateAccess(Visitor visitor) {
        JwtAuthPrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.hasAnyRole(RoleConstants.SYSTEM_ADMINISTRATOR, RoleConstants.APARTMENT_MANAGER, RoleConstants.SECURITY_OFFICER)) {
            return;
        }
        if (!visitor.getResidentUserId().equals(principal.getUserId()) && !visitor.getCreatedByUserId().equals(principal.getUserId())) {
            throw new ForbiddenException("Access denied to visitor record");
        }
    }

    private void recordHistory(String visitorId, String event, VisitorStatus prevStatus, VisitorStatus newStatus, String reason, String actor) {
        VisitorHistory history = VisitorHistory.builder()
                .visitorId(visitorId)
                .event(event)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(actor)
                .changedAt(Instant.now())
                .requestId(SecurityUtils.getCurrentRequestId())
                .build();
        visitorHistoryRepository.save(history);
    }
}
