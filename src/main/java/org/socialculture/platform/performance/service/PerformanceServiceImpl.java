package org.socialculture.platform.performance.service;

import lombok.RequiredArgsConstructor;
import org.socialculture.platform.coupon.dto.response.CouponResponseDto;
import org.socialculture.platform.coupon.entity.CouponEntity;
import org.socialculture.platform.coupon.repository.CouponRepository;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.member.entity.MemberEntity;
import org.socialculture.platform.member.repository.MemberRepository;
import org.socialculture.platform.member.service.MemberService;
import org.socialculture.platform.performance.dto.CategoryDto;
import org.socialculture.platform.performance.dto.domain.PerformanceDetail;
import org.socialculture.platform.performance.dto.domain.PerformanceWithCategory;
import org.socialculture.platform.performance.dto.request.PerformanceRegisterRequest;
import org.socialculture.platform.performance.dto.response.PerformanceRegisterResponse;
import org.socialculture.platform.performance.dto.request.PerformanceUpdateRequest;
import org.socialculture.platform.performance.dto.response.PerformanceDetailResponse;
import org.socialculture.platform.performance.dto.response.PerformanceListResponse;
import org.socialculture.platform.performance.dto.response.PerformanceUpdateResponse;
import org.socialculture.platform.performance.entity.CategoryEntity;
import org.socialculture.platform.performance.entity.PerformanceCategoryEntity;
import org.socialculture.platform.performance.entity.PerformanceEntity;
import org.socialculture.platform.performance.repository.CategoryRepository;
import org.socialculture.platform.performance.repository.PerformanceCategoryRepository;
import org.socialculture.platform.performance.repository.PerformanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.socialculture.platform.global.apiResponse.exception.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private static final int FIRST_COME_COUPON_LIMIT = 3;

    private final PerformanceRepository performanceRepository;
    private final PerformanceCategoryRepository performanceCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final CouponRepository couponRepository;


    private final ImageUploadService imageUploadService;
    //TODO : 사용자 넣기
    @Transactional
    @Override
    public PerformanceRegisterResponse registerPerformance(String email, PerformanceRegisterRequest performanceRegisterRequest, MultipartFile imageFile) {
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MEMBER_NOT_FOUND));
        PerformanceEntity performanceEntity = performanceRegisterRequest.toEntity(performanceRegisterRequest);
        performanceEntity.updateMember(memberEntity);
        String imageUrl = imageUploadService.uploadFileToFTP(imageFile);
        performanceEntity.updateImageUrl(imageUrl);
        performanceEntity = performanceRepository.save(performanceEntity);

        //공연별 선착순 쿠폰 3장 생성
        if (performanceEntity.getMaxAudience() > FIRST_COME_COUPON_LIMIT) {
            createFirstComeCoupon(performanceEntity);
        }

        List<CategoryEntity> categoryEntities = performanceCategorySave(performanceEntity, performanceRegisterRequest.categories());
        List<CategoryDto> categoryDtos = categoryEntities.stream()
                .map(CategoryDto::toDto).toList();

        return PerformanceRegisterResponse.of(performanceEntity, categoryDtos);
    }

    @Override
    public PerformanceListResponse getPerformanceList(Integer page, Integer size, Long categoryId, String search, String email) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PerformanceWithCategory> performanceList =
                performanceRepository.getPerformanceWithCategoryList(pageRequest, categoryId, search, email);

        return PerformanceListResponse.from(performanceList.getTotalElements(), performanceList.getContent());
    }

    @Override
    public PerformanceDetailResponse getPerformanceDetail(String email, Long performanceId) {
        if (isAccessPerformance(email, performanceId)) {
            //선착순 쿠폰
            List<CouponResponseDto> firstComeCouponDtos = couponRepository.findByPerformance_PerformanceId(performanceId)
                    .stream()
                    .map(CouponResponseDto::fromEntity)
                    .toList();

            PerformanceDetail performanceDetail = performanceRepository.getPerformanceDetail(performanceId)
                    .orElseThrow(() -> new GeneralException(PERFORMANCE_NOT_FOUND));

            performanceDetail.updateFirstComeCoupons(firstComeCouponDtos);

            return PerformanceDetailResponse.from(true, performanceDetail);
        }

        return performanceRepository.getPerformanceDetail(performanceId)
                .map(performanceDetail -> PerformanceDetailResponse.from(false, performanceDetail))
                .orElseThrow(() -> new GeneralException(PERFORMANCE_NOT_FOUND));
    }

    // Todo : 사용자 이메일과 같은지 확인 절차 추가할 것
    @Override
    @Transactional
    public PerformanceUpdateResponse updatePerformance(String email, Long performanceId, PerformanceUpdateRequest performanceUpdateRequest) {
        if (!isAccessPerformance(email, performanceId)) {
            throw new GeneralException(PERFORMANCE_NOT_ACCESSIBLE);
        }

        PerformanceEntity performanceEntity = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new GeneralException(PERFORMANCE_NOT_FOUND));

        performanceEntity.updatePerformance(PerformanceUpdateRequest.toEntity(performanceUpdateRequest));

        return PerformanceUpdateResponse.from(performanceEntity.getPerformanceId());
    }

    @Override
    @Transactional
    public void deletePerformance(String email, Long performanceId) {
        if (isAccessPerformance(email, performanceId)) {
            throw new GeneralException(PERFORMANCE_NOT_ACCESSIBLE);
        }

        PerformanceEntity performanceEntity = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PERFORMANCE_NOT_FOUND));

        performanceEntity.updateDeleteAt();
    }

    private List<CategoryEntity> performanceCategorySave(PerformanceEntity performanceEntity, List<Long> categories) {
        List<CategoryEntity> categoryEntities = categoryRepository.findAllById(categories); // 한 번에 조회

        if (categoryEntities.isEmpty()) {
            return new ArrayList<>();
        }

        categoryEntities.forEach(e ->
                performanceCategoryRepository.save(PerformanceCategoryEntity.of(performanceEntity, e)));

        return categoryEntities;
    }

    @Override
    public PerformanceListResponse getMyPerformanceList(String email, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PerformanceWithCategory> performanceList = performanceRepository.getMyPerformanceWithCategoryList(email, pageRequest);

        if (performanceList.isEmpty()) {
            throw new GeneralException(PERFORMANCE_NOT_FOUND);
        }

        return PerformanceListResponse.from(performanceList.getTotalElements(), performanceList.getContent());
    }

    @Override
    public List<CategoryDto> getCategoryList() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        return categoryEntities.stream()
                .map(CategoryDto::toDto).toList();
    }


    // 사용자 선호 카테고리 기반 추천 공연 조회
    @Override
    public PerformanceListResponse getPerformanceListByUserCategories(String email) {
        Long memberId = memberService.getMemberIdByEmail(email);
        List<PerformanceWithCategory> recommendedPerformancesByMember = performanceRepository
                .getRecommendedPerformancesByMember(memberId);

        return PerformanceListResponse.from(recommendedPerformancesByMember.size(),
                recommendedPerformancesByMember);
    }





    private boolean isAccessPerformance(String email, Long performanceId) {
        if (email == null) {
            return false;
        }

        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MEMBER_NOT_FOUND));

        PerformanceEntity performanceEntity = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new GeneralException(PERFORMANCE_NOT_FOUND));

        return memberEntity.getEmail().equals(performanceEntity.getMember().getEmail());
    }

    private void createFirstComeCoupon(PerformanceEntity performanceEntity) {
        for (int i = 1; i <= FIRST_COME_COUPON_LIMIT; i++) {
            CouponEntity couponEntity = CouponEntity.builder()
                    .name("선착순 10% 할인 쿠폰 " + i)
                    .percent(10)
                    .performance(performanceEntity)
                    .isUsed(false)
                    .build();

            couponRepository.save(couponEntity);
        }
    }
}
