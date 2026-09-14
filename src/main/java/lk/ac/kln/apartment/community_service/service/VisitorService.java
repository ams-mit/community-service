package lk.ac.kln.apartment.community_service.service;

import lk.ac.kln.apartment.community_service.dto.VisitorRequest;
import lk.ac.kln.apartment.community_service.dto.VisitorResponse;
import lk.ac.kln.apartment.community_service.entity.Visitor;
import lk.ac.kln.apartment.community_service.entity.VisitorStatus;
import lk.ac.kln.apartment.community_service.repository.VisitorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    public VisitorResponse registerVisitor(VisitorRequest request) {

        Visitor visitor = new Visitor();
        visitor.setVisitorName(request.getVisitorName());
        visitor.setResidentId(request.getResidentId());
        visitor.setUnitId(request.getUnitId());
        visitor.setPurpose(request.getPurpose());
        visitor.setVisitDate(request.getVisitDate());
        visitor.setStatus(VisitorStatus.EXPECTED);

        Visitor saved = visitorRepository.save(visitor);

        return mapToResponse(saved);
    }

    public VisitorResponse checkInVisitor(Long visitorId) {

        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Visitor not found"));

        if (visitor.getStatus() == VisitorStatus.CHECKED_IN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Visitor is already checked in");
        }

        visitor.setStatus(VisitorStatus.CHECKED_IN);
        visitor.setCheckedInAt(LocalDateTime.now());

        Visitor saved = visitorRepository.save(visitor);

        return mapToResponse(saved);
    }

    private VisitorResponse mapToResponse(Visitor visitor) {
        return new VisitorResponse(
                visitor.getId(),
                visitor.getVisitorName(),
                visitor.getResidentId(),
                visitor.getUnitId(),
                visitor.getPurpose(),
                visitor.getVisitDate(),
                visitor.getStatus(),
                visitor.getCheckedInAt(),
                visitor.getCreatedAt()
        );
    }
}