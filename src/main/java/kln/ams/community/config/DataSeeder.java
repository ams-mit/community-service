package kln.ams.community.config;

import kln.ams.community.entity.*;
import kln.ams.community.repository.AnnouncementAudienceRepository;
import kln.ams.community.repository.AnnouncementRepository;
import kln.ams.community.repository.NotificationRepository;
import kln.ams.community.repository.VisitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final VisitorRepository visitorRepository;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAudienceRepository audienceRepository;
    private final NotificationRepository notificationRepository;

    public DataSeeder(
            VisitorRepository visitorRepository,
            AnnouncementRepository announcementRepository,
            AnnouncementAudienceRepository audienceRepository,
            NotificationRepository notificationRepository) {
        this.visitorRepository = visitorRepository;
        this.announcementRepository = announcementRepository;
        this.audienceRepository = audienceRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (visitorRepository.count() == 0) {
            log.info("Seeding initial synthetic visitors...");
            Visitor v1 = Visitor.builder()
                    .id("a0000000-0000-0000-0000-000000000001")
                    .residentUserId("u0000000-0000-0000-0000-000000000001")
                    .unitId("unit-101-uuid")
                    .visitorName("Nimal Perera")
                    .visitorContact("0770000001")
                    .visitDate(LocalDate.now().plusDays(1))
                    .expectedArrival("10:00")
                    .expectedDeparture("12:00")
                    .purpose("Family Visit")
                    .status(VisitorStatus.PENDING)
                    .notes("Arriving by car")
                    .createdByUserId("u0000000-0000-0000-0000-000000000001")
                    .build();

            Visitor v2 = Visitor.builder()
                    .id("a0000000-0000-0000-0000-000000000002")
                    .residentUserId("u0000000-0000-0000-0000-000000000001")
                    .unitId("unit-101-uuid")
                    .visitorName("Kamal Silva")
                    .visitorContact("0770000002")
                    .visitDate(LocalDate.now())
                    .expectedArrival("14:00")
                    .expectedDeparture("16:00")
                    .purpose("Delivery")
                    .status(VisitorStatus.APPROVED)
                    .createdByUserId("u0000000-0000-0000-0000-000000000001")
                    .build();

            visitorRepository.save(v1);
            visitorRepository.save(v2);
        }

        if (announcementRepository.count() == 0) {
            log.info("Seeding initial synthetic announcements...");
            Announcement a1 = Announcement.builder()
                    .id("b0000000-0000-0000-0000-000000000001")
                    .title("Water Supply Maintenance")
                    .content("Water supply will be temporarily interrupted tomorrow from 10:00 to 12:00.")
                    .category("MAINTENANCE")
                    .priority("HIGH")
                    .status(AnnouncementStatus.PUBLISHED)
                    .audienceType(AudienceType.ALL_RESIDENTS)
                    .publishedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .createdByUserId("admin-uuid")
                    .build();

            announcementRepository.save(a1);
        }

        if (notificationRepository.count() == 0) {
            log.info("Seeding initial synthetic notifications...");
            Notification n1 = Notification.builder()
                    .id("c0000000-0000-0000-0000-000000000001")
                    .recipientUserId("u0000000-0000-0000-0000-000000000001")
                    .type("MAINTENANCE_REQUEST_UPDATED")
                    .title("Maintenance Request Updated")
                    .message("Your maintenance request #MR-101 is now in progress.")
                    .priority("NORMAL")
                    .status(NotificationStatus.UNREAD)
                    .deliveryStatus("DELIVERED")
                    .sourceService("operations-service")
                    .sourceEntityType("MaintenanceRequest")
                    .sourceEntityId("mr-101-uuid")
                    .build();

            notificationRepository.save(n1);
        }
    }
}
