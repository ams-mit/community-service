package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.VisitorRequest;
import lk.ac.kln.apartment.community_service.dto.VisitorResponse;
import lk.ac.kln.apartment.community_service.entity.Visitor;
import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import lk.ac.kln.apartment.community_service.repository.VisitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.kln.apartment.community_service.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.stream.Collectors;

@Service
public class VisitorService {

    private static final Logger log = LoggerFactory.getLogger(VisitorService.class);

    private final VisitorRepository visitorRepository;
    private final NotificationService notificationService;

    @Autowired
    public VisitorService(VisitorRepository visitorRepository, NotificationService notificationService) {
        this.visitorRepository = visitorRepository;
        this.notificationService = notificationService;
    }

    public VisitorService(VisitorRepository visitorRepository) {
        this(visitorRepository, null);
    }

    public List<VisitorResponse> getVisitors(String residentId, VisitorStatus status, LocalDate visitDate) {
        return visitorRepository.findFiltered(residentId, status, visitDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VisitorResponse getVisitorById(Long id) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found with id: " + id));
        return mapToResponse(visitor);
    }

    @Transactional
    public VisitorResponse registerVisitor(VisitorRequest request) {
        Visitor visitor = new Visitor();
        visitor.setVisitorName(request.getVisitorName());
        visitor.setResidentId(request.getResidentId());
        visitor.setUnitId(request.getUnitId());
        visitor.setPurpose(request.getPurpose());
        visitor.setVisitDate(request.getVisitDate());
        visitor.setVisitorPhone(request.getVisitorPhone());
        visitor.setVehicleNumber(request.getVehicleNumber());
        visitor.setStatus(VisitorStatus.EXPECTED);

        // Generate a clean, unique pass code
        String uniqueCode = "PASS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        visitor.setPassCode(uniqueCode);

        Visitor saved = visitorRepository.save(visitor);
        return mapToResponse(saved);
    }

    @Transactional
    public VisitorResponse checkInVisitor(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Visitor not found with id: " + visitorId));

        if (visitor.getStatus() == VisitorStatus.CHECKED_IN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Visitor is already checked in");
        }

        if (visitor.getStatus() != VisitorStatus.EXPECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot check in visitor with status: " + visitor.getStatus());
        }

        visitor.setStatus(VisitorStatus.CHECKED_IN);
        visitor.setCheckedInAt(LocalDateTime.now());

        Visitor saved = visitorRepository.save(visitor);

        if (notificationService != null) {
            try {
                NotificationRequest notif = new NotificationRequest();
                notif.setRecipientId(saved.getResidentId());
                notif.setRecipientRole("RESIDENT");
                notif.setTitle("Visitor Arrival: " + saved.getVisitorName());
                notif.setSummary(saved.getVisitorName() + " has arrived and checked in at security gate for Unit " + saved.getUnitId() + ".");
                notif.setMessage(notif.getSummary());
                notif.setType("security");
                notif.setCategory("SECURITY");
                notif.setPriority("high");
                notif.setIssuedBy("Resident Security & Access Control");
                notif.setAffectedArea("Main Security Gate");
                notif.setActionRoute("/visitors");
                notif.setActionLabel("View Visitor");
                notificationService.createNotification(notif);
            } catch (Exception ignored) {
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public VisitorResponse checkOutVisitor(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Visitor not found with id: " + visitorId));

        if (visitor.getStatus() != VisitorStatus.CHECKED_IN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only checked-in visitors can be checked out");
        }

        visitor.setStatus(VisitorStatus.CHECKED_OUT);
        visitor.setCheckedOutAt(LocalDateTime.now());

        Visitor saved = visitorRepository.save(visitor);
        return mapToResponse(saved);
    }

    @Transactional
    public VisitorResponse cancelVisitor(Long visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Visitor not found with id: " + visitorId));

        if (visitor.getStatus() != VisitorStatus.EXPECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only EXPECTED visitors can be cancelled");
        }

        visitor.setStatus(VisitorStatus.CANCELLED);
        Visitor saved = visitorRepository.save(visitor);
        return mapToResponse(saved);
    }

    public VisitorResponse verifyPassCode(String passCode) {
        Visitor visitor = visitorRepository.findByPassCode(passCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid or unrecognized gate pass code: " + passCode));
        return mapToResponse(visitor);
    }

    /**
     * Cron task running every hour to expire unvisited EXPECTED visitors whose visitDate has passed.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOverdueExpectedVisitors() {
        LocalDate today = LocalDate.now();
        List<Visitor> overdue = visitorRepository.findByStatusAndVisitDateBefore(VisitorStatus.EXPECTED, today);
        if (!overdue.isEmpty()) {
            for (Visitor v : overdue) {
                v.setStatus(VisitorStatus.EXPIRED);
            }
            visitorRepository.saveAll(overdue);
            log.info("Expired {} unvisited visitor passes past their scheduled visit date.", overdue.size());
        }
    }

    public VisitorResponse mapToResponse(Visitor visitor) {
        return new VisitorResponse(
                visitor.getId(),
                visitor.getVisitorName(),
                visitor.getResidentId(),
                visitor.getUnitId(),
                visitor.getPurpose(),
                visitor.getVisitDate(),
                visitor.getVisitorPhone(),
                visitor.getVehicleNumber(),
                visitor.getPassCode(),
                visitor.getStatus(),
                visitor.getCheckedInAt(),
                visitor.getCheckedOutAt(),
                visitor.getCreatedAt()
        );
    }
}