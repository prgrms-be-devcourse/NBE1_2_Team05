package org.socialculture.platform.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socialculture.platform.global.entity.BaseEntity;
import org.socialculture.platform.member.entity.MemberEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "performance")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long performanceId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "date_start_time")
    private LocalDateTime dateStartTime;

    @Column(name = "date_end_time")
    private LocalDateTime dateEndTime;

    @Column(name = "description")
    private String description;

    @Column(name = "max_audience")
    private Integer maxAudience;

    @Column(name = "address")
    private String address;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price")
    private Integer price;

    @Column(name = "remaining_tickets")
    private int remainingTickets;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PerformanceStatus performanceStatus;

    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PerformanceCategoryEntity> performanceCategoryList = new ArrayList<>();

    public void updatePerformance(PerformanceEntity performanceEntity) {
        if (performanceEntity.getTitle() != null) {
            this.title = performanceEntity.getTitle();
        }
        if (performanceEntity.getDateStartTime() != null) {
            this.dateStartTime = performanceEntity.getDateStartTime();
        }
        if (performanceEntity.getDateEndTime() != null) {
            this.dateEndTime = performanceEntity.getDateEndTime();
        }
        if (performanceEntity.getDescription() != null) {
            this.description = performanceEntity.getDescription();
        }
        if (performanceEntity.getAddress() != null) {
            this.address = performanceEntity.getAddress();
        }
        if (performanceEntity.getImageUrl() != null) {
            this.imageUrl = performanceEntity.getImageUrl();
        }
        if (performanceEntity.getPrice() != null) {
            this.price = performanceEntity.getPrice();
        }
        if (performanceEntity.getMaxAudience() != null) {
            this.maxAudience = performanceEntity.getMaxAudience();
        }
        if (performanceEntity.getPerformanceStatus() != null) {
            this.performanceStatus = performanceEntity.getPerformanceStatus();
        }
    }

    public void updateMember(MemberEntity memberEntity) {
        this.member = memberEntity;
    }

    public void updateDeleteAt() {
        this.recordDeletedAt(LocalDateTime.now());
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
