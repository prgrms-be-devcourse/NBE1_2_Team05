package org.socialculture.platform.performance.service;

import org.socialculture.platform.performance.dto.CategoryDto;
import org.socialculture.platform.performance.dto.request.PerformanceRegisterRequest;
import org.socialculture.platform.performance.dto.response.PerformanceRegisterResponse;
import org.socialculture.platform.performance.dto.request.PerformanceUpdateRequest;
import org.socialculture.platform.performance.dto.response.PerformanceDetailResponse;
import org.socialculture.platform.performance.dto.response.PerformanceListResponse;
import org.socialculture.platform.performance.dto.response.PerformanceUpdateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PerformanceService {
    PerformanceRegisterResponse registerPerformance(String email, PerformanceRegisterRequest performanceRegisterRequest, MultipartFile imageFile);

    PerformanceListResponse getPerformanceList(Integer page, Integer size, Long categoryId, String search, String email);

    PerformanceDetailResponse getPerformanceDetail(String email, Long performanceId);

    PerformanceUpdateResponse updatePerformance(String email, Long performanceId, PerformanceUpdateRequest performanceUpdateRequest);

    void deletePerformance(String email, Long performanceId);

    PerformanceListResponse getMyPerformanceList(String email, Integer page, Integer size);

    List<CategoryDto> getCategoryList();
}
