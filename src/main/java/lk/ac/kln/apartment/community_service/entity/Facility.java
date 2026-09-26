package lk.ac.kln.apartment.community_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "facilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer capacity;

    private String location;

    @Column(name = "operating_hours_start")
    private LocalTime operatingHoursStart;

    @Column(name = "operating_hours_end")
    private LocalTime operatingHoursEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "rules_and_guidelines", length = 2000)
    private String rulesAndGuidelines;

    @Column(name = "booking_fee")
    private Double bookingFee;

    @Column(name = "max_hours_per_booking")
    private Integer maxHoursPerBooking;
}