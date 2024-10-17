package org.socialculture.platform.performance.dto.request;

import lombok.Builder;
import org.socialculture.platform.performance.entity.PerformanceEntity;
import org.socialculture.platform.performance.entity.PerformanceStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PerformanceRegisterRequest(
        String title,
        LocalDateTime dateStartTime,
        LocalDateTime dateEndTime,
        String address,
        String imageUrl,
        Integer price,
        String description,
        Integer maxAudience,
        LocalDateTime startDate,
        List<String> categories
) {
    public PerformanceRegisterRequest {
        // price가 null이면 기본값으로 0을 설정
        if (price == null) {
            price = 0;
        }
        // maxAudience가 null이면 기본값으로 0을 설정
        if (maxAudience == null) {
            maxAudience = 0;
        }
    }

    public PerformanceEntity toEntity(PerformanceRegisterRequest performanceRegisterRequest) {
        return PerformanceEntity.builder()
                .title(performanceRegisterRequest.title)
                .dateStartTime(performanceRegisterRequest.dateStartTime)
                .dateEndTime(performanceRegisterRequest.dateEndTime)
                .description(performanceRegisterRequest.description)
                .maxAudience(performanceRegisterRequest.maxAudience)
                .address(performanceRegisterRequest.address)
                .imageUrl(performanceRegisterRequest.imageUrl)
                .price(performanceRegisterRequest.price)
                .remainingTickets(performanceRegisterRequest.maxAudience)
                .startDate(performanceRegisterRequest.startDate)
                .performanceStatus(PerformanceStatus.NOT_CONFIRMED)
                .build();
    }

    public PerformanceRegisterRequest withImageUrl(String imageUrl) {
        return new PerformanceRegisterRequest(
                this.title,
                this.dateStartTime,
                this.dateEndTime,
                this.address,
                imageUrl, // 새로운 imageUrl 값
                this.price,
                this.description,
                this.maxAudience,
                this.startDate,
                this.categories
        );
    }
}
