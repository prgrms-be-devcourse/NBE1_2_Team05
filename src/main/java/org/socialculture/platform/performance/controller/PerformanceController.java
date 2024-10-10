package org.socialculture.platform.performance.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.socialculture.platform.global.apiResponse.ApiResponse;
import org.socialculture.platform.performance.dto.request.PerformanceRegisterRequest;
import org.socialculture.platform.performance.dto.response.PerformanceRegisterResponse;
import org.socialculture.platform.performance.dto.request.PerformanceUpdateRequest;
import org.socialculture.platform.performance.dto.response.PerformanceDetailResponse;
import org.socialculture.platform.performance.dto.response.PerformanceListResponse;
import org.socialculture.platform.performance.dto.response.PerformanceUpdateResponse;
import org.socialculture.platform.performance.service.PerformanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * @author Icecoff22
     * @param registerPerformanceRequest
     * @return 200, 등록 완료 메세지
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PerformanceRegisterResponse>> registerPerformance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerformanceRegisterRequest registerPerformanceRequest
    ) {
        PerformanceRegisterResponse performanceRegisterResponse =
                performanceService.registerPerformance(userDetails.getUsername(), registerPerformanceRequest);

        return ApiResponse.onSuccess(
                HttpStatus.CREATED,
                "PERFORMANCE201",
                "공연이 등록되었습니다.",
                performanceRegisterResponse
        );
    }

    /**
     * 공연에 대한 전반적인 정보를 모두 조회.
     * 단, 확정되지 않은 공연은 조회하지 않는다.
     * @author Icecoff22
     * @param page
     * @param size
     * @return 200, 공연응답 리스트
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PerformanceListResponse>> getPerformanceList(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size,
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "search", required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ApiResponse.onSuccess(performanceService.getPerformanceList(page, size, categoryId, search, null));
        }
        return ApiResponse.onSuccess(performanceService.getPerformanceList(page, size, categoryId, search, userDetails.getUsername()));
    }

    /**
     * 특정 공연에 대한 전반적인 정보를 모두 조회.
     * @author Icecoff22
     * @param performanceId
     * @return 200, 공연응답 리스트
     */
    @GetMapping("/{performanceId}")
    public ResponseEntity<ApiResponse<PerformanceDetailResponse>> getPerformanceById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("performanceId") Long performanceId
    ) {
        return ApiResponse.onSuccess(performanceService.getPerformanceDetail(userDetails.getUsername(), performanceId));
    }

    /**
     * 공연에 대한 정보를 수정.
     * 자신의 공연만 수정할 수 있다.
     * @author Icecoff22
     * @param performanceId
     * @return 200, 공연수정 정보
     */
    @PatchMapping("/{performanceId}")
    public ResponseEntity<ApiResponse<PerformanceUpdateResponse>> updatePerformance(
            @PathVariable("performanceId") Long performanceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerformanceUpdateRequest performanceUpdateRequest
    ) {
        return ApiResponse.onSuccess(performanceService.updatePerformance(userDetails.getUsername(), performanceId, performanceUpdateRequest));
    }

    /**
     * 공연에 대한 삭제.
     * 실 삭제가 아닌 deleteAt만 업데이트 시킨다.
     * 자신의 공연만 삭제할 수 있다.
     * @author Icecoff22
     * @param performanceId
     * @return 200
     */
    @DeleteMapping("/{performanceId}")
    public ResponseEntity<ApiResponse<Void>> deletePerformance(
            @PathVariable("performanceId") Long performanceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        performanceService.deletePerformance(userDetails.getUsername(), performanceId);
        return ApiResponse.onSuccess();
    }

    @GetMapping("/admin/my")
    public ResponseEntity<ApiResponse<PerformanceListResponse>> getPerformanceListAdmin(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.onSuccess(performanceService.getMyPerformanceList(userDetails.getUsername(), page, size));
    }
}
