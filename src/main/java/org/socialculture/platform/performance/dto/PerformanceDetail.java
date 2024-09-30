package org.socialculture.platform.performance.dto;

import lombok.Getter;
import org.socialculture.platform.performance.entity.PerformanceStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PerformanceDetail {
    private String memberName;
    private Long performanceId;
    private String title;
    private LocalDateTime dateStartTime;
    private LocalDateTime dateEndTime;
    private String description;
    private int maxAudience;
    private String address;
    private String imageUrl;
    private int price;
    private int remainingTickets;
    private LocalDateTime startDate;
    private PerformanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryDTO> categories;

    PerformanceDetail() {}
    PerformanceDetail(String memberName, Long performanceId, String title, LocalDateTime dateStartTime, LocalDateTime dateEndTime, String description,
                      int maxAudience, String address, String imageUrl, int price, int remainingTickets, LocalDateTime startDate, PerformanceStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.memberName = memberName;
        this.performanceId = performanceId;
        this.title = title;
        this.dateStartTime = dateStartTime;
        this.dateEndTime = dateEndTime;
        this.description = description;
        this.maxAudience = maxAudience;
        this.address = address;
        this.imageUrl = imageUrl;
        this.price = price;
        this.remainingTickets = remainingTickets;
        this.startDate = startDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }
}
