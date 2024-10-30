package org.socialculture.platform.member.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.member.dto.request.LocalRegisterRequest;
import org.socialculture.platform.member.dto.request.MemberCategoryRequest;
import org.socialculture.platform.member.dto.request.SocialRegisterRequest;
import org.socialculture.platform.member.dto.response.CategoryResponse;
import org.socialculture.platform.member.dto.response.MemberInfoResponse;
import org.socialculture.platform.member.entity.MemberCategoryEntity;
import org.socialculture.platform.member.entity.MemberEntity;
import org.socialculture.platform.member.entity.MemberRole;
import org.socialculture.platform.member.entity.SocialProvider;
import org.socialculture.platform.member.oauth.common.dto.SocialMemberCheckDto;
import org.socialculture.platform.member.repository.MemberCategoryRepository;
import org.socialculture.platform.member.repository.MemberRepository;
import org.socialculture.platform.performance.entity.CategoryEntity;
import org.socialculture.platform.performance.repository.CategoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidatorService memberValidatorService;
    private final CategoryRepository categoryRepository;
    private final MemberCategoryRepository memberCategoryRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * 일반 사용자 회원가입
     * 패스워드 암호화 하여 저장
     *
     * @param localRegisterRequest
     */
    @Override
    @Transactional
    public void registerBasicUser(LocalRegisterRequest localRegisterRequest) {
        validateEmailAndCheckDuplicate(localRegisterRequest.email());
        validateNameAndCheckDuplicate(localRegisterRequest.name());
        memberValidatorService.validatePassword(localRegisterRequest.password());

        String encodedPassword = passwordEncoder.encode(localRegisterRequest.password());
        System.out.println(encodedPassword);
        MemberEntity memberEntity = MemberEntity.builder()
                .email(localRegisterRequest.email())
                .password(encodedPassword)  // 암호화된 패스워드 저장
                .name(localRegisterRequest.name())
                .role(MemberRole.ROLE_USER)
                .provider(SocialProvider.LOCAL)
                .build();

        memberRepository.save(memberEntity);
    }


    /**
     * 소셜 사용자가 가입되어있는지 확인
     *
     * @param socialMemberCheckDto
     * @return true이면 가입되어있는 사용자, false면 가입되어있지 않는 사용자
     */
    @Override
    public boolean isSocialMemberRegistered(SocialMemberCheckDto socialMemberCheckDto) {

        Optional<MemberEntity> findMember = memberRepository.findByEmail(socialMemberCheckDto.email());

        return findMember.map(member -> {
            if (member.getProviderId() == null) {
                log.info("이메일이 이미 존재하지만 소셜 회원은 아닐때");
                throw new GeneralException(ErrorStatus.EMAIL_DUPLICATE);
            }
            boolean matchesProvider = member.getProviderId().equals(socialMemberCheckDto.providerId());
            if (!matchesProvider) {
                log.info("이메일이 이미 존재하고 소셜 회원일때");
                throw new GeneralException(ErrorStatus.SOCIAL_EMAIL_DUPLICATE);
            }
            return true;
        }).orElse(false);
    }


    /**
     * 소셜 사용자의 닉네임 체크 후 회원가입
     *
     * @param request
     * @param session
     */
    @Transactional
    @Override
    public void registerSocialMember(SocialRegisterRequest request, HttpSession session) {

        validateEmailAndCheckDuplicate(request.email());
        MemberEntity memberEntity = request.toEntity();
        memberRepository.save(memberEntity);

        session.invalidate(); // 회원가입용 임시 회원 정보세션 종료
    }


    /**
     * 이메일 중복 확인(이메일 형식 검증도 같이확인)
     *
     * @param email
     */
    @Override
    public void validateEmailAndCheckDuplicate(String email) {
        memberValidatorService.validateEmail(email);
        boolean emailExists = memberRepository.existsByEmail(email);
        if (emailExists) {
            throw new GeneralException(ErrorStatus.EMAIL_DUPLICATE);
        }
    }


    /**
     * 닉네임 중복 확인 (닉네임 형식 검증도 같이확인)
     *
     * @param name
     */
    @Override
    public void validateNameAndCheckDuplicate(String name) {
        memberValidatorService.validateName(name);
        boolean nameExists = memberRepository.existsByName(name);
        if (nameExists) {
            throw new GeneralException(ErrorStatus.NAME_DUPLICATE);
        }
    }


    /**
     * 닉네임 변경
     *
     * @param email
     * @param name
     */
    @Override
    public void updateName(String email, String name) {
        Optional<MemberEntity> byEmail = memberRepository.findByEmail(email);

        byEmail.ifPresentOrElse(member -> {
            validateNameAndCheckDuplicate(name);
            member.changeName(name);
            memberRepository.save(member);
        }, () -> {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        });
    }


    /**
     * 사용자 선호 카테고리 추가
     *
     * @param memberCategoryRequest
     * @param email
     */
    @Override
    @Transactional
    public void memberAddCategory(MemberCategoryRequest memberCategoryRequest, String email) {
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        addCategories(memberCategoryRequest, memberEntity);
    }


    /**
     * 선호 카테고리 수정
     *
     * @param memberCategoryRequest
     * @param email
     */
    @Transactional
    @Override
    public void updateFavoriteCategories(MemberCategoryRequest memberCategoryRequest,
                                         String email) {

        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow((() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND)));

        memberCategoryRepository.deleteByMemberMemberId(memberEntity.getMemberId());
        memberAddCategory(memberCategoryRequest, email);

    }


    // 받은 카테고리 목록을 추가
    private void addCategories(MemberCategoryRequest memberCategoryRequest, MemberEntity memberEntity) {
        List<Long> categoryIds = memberCategoryRequest.categories();
        for (Long categoryId : categoryIds) {
            CategoryEntity categoryEntity = categoryRepository.findByCategoryId(categoryId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_CATEGORY_NOT_FOUND));

            MemberCategoryEntity memberCategoryEntity = MemberCategoryEntity.builder()
                    .category(categoryEntity)
                    .member(memberEntity)
                    .build();

            memberCategoryRepository.save(memberCategoryEntity);
        }
    }


    /**
     * 카테고리 전체 목록 조회
     *
     * @return categoryId, categoryName
     */
    @Override
    public List<CategoryResponse> getAllCategories() {
        List<CategoryEntity> categoryEntityList = categoryRepository.findAll();

        return categoryEntityList.stream().map(CategoryResponse::fromEntity).toList();
    }


    /**
     * 사용자 선호 카테고리 조회
     *
     * @param email
     * @return 선호하는 카테고리 목록
     */
    @Override
    public List<CategoryResponse> getFavoriteCategories(String email) {
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<CategoryEntity> favoriteCategories = memberCategoryRepository
                .findCategoriesByMemberId(memberEntity.getMemberId());

        return favoriteCategories.stream().map(CategoryResponse::fromEntity).toList();
    }


    /**
     * 사용자 정보 조회
     *
     * @param email
     * @return email, name, role
     */
    @Override
    public MemberInfoResponse getMemberInfoByEmail(String email) {
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        return MemberInfoResponse.fromEntity(memberEntity);
    }


    /**
     * 공연관리자로 권한 변경
     *
     * @param email
     */
    @Override
    @Transactional
    public void changeRoleToPadmin(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        member.changeRole(MemberRole.ROLE_PADMIN);
        memberRepository.save(member);
    }


    /**
     * 사용자 첫 로그인 후 첫 로그인 여부 변경
     *
     * @param email
     */
    @Override
    public void changeFirstLogin(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        member.markFirstLoginComplete();
        memberRepository.save(member);
    }

    /**
     * 사용자의 이메일을 가지고 memberId를 조회
     *
     * @param email
     * @return
     */
    @Override
    public Long getMemberIdByEmail(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return member.getMemberId();
    }


}
