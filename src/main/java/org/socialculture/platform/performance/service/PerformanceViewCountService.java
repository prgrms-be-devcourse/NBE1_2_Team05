package org.socialculture.platform.performance.service;

import org.socialculture.platform.performance.dto.response.PerformanceListResponse;

import java.util.List;

public interface PerformanceViewCountService {
    void incrementViewCount(Long performanceId);


    PerformanceListResponse getPopularPerformances();
}
