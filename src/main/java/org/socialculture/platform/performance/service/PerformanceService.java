package org.socialculture.platform.performance.service;

import org.socialculture.platform.performance.dto.request.PerformanceUpdateRequest;
import org.socialculture.platform.performance.dto.response.PerformanceDetailResponse;
import org.socialculture.platform.performance.dto.response.PerformanceListResponse;
import org.socialculture.platform.performance.dto.response.PerformanceUpdateResponse;

import java.util.List;

public interface PerformanceService {
    List<PerformanceListResponse> getPerformanceList(Integer page, Integer size);

    PerformanceDetailResponse getPerformanceDetail(Long performanceId);

    PerformanceUpdateResponse updatePerformance(Long performanceId, PerformanceUpdateRequest performanceUpdateRequest);
}