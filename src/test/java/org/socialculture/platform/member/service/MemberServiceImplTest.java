package org.socialculture.platform.member.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.member.dto.request.LocalRegisterRequest;
import org.socialculture.platform.member.dto.request.MemberCategoryRequest;
import org.socialculture.platform.member.entity.MemberCategoryEntity;
import org.socialculture.platform.member.entity.MemberEntity;
import org.socialculture.platform.member.entity.SocialProvider;
import org.socialculture.platform.member.oauth.common.dto.SocialMemberCheckDto;
import org.socialculture.platform.member.repository.MemberCategoryRepository;
import org.socialculture.platform.member.repository.MemberRepository;
import org.socialculture.platform.performance.entity.CategoryEntity;
import org.socialculture.platform.performance.repository.CategoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberValidatorService memberValidatorService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberCategoryRepository memberCategoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("일반 유저 회원가입 성공")
    void registerBasicUserSuccess() {
        //given
        String email = "test@test.com";
        String name = "testUser";
        String password = "qwer1234!";
        String encodedPassword = "encodedPassword";

        LocalRegisterRequest request = new LocalRegisterRequest(email, password, name);

        doNothing().when(memberValidatorService).validateEmail(email);
        doNothing().when(memberValidatorService).validateName(name);
        doNothing().when(memberValidatorService).validatePassword(password);
        when(memberRepository.existsByEmail(email)).thenReturn(false);
        when(memberRepository.existsByName(name)).thenReturn(false);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        //when
        memberService.registerBasicUser(request);

        //then
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }


    @Test
    @DisplayName("소셜 사용자가 이미 가입되어 있는 경우")
    void isSocialMemberRegisteredReturnTrue() {
        //given
        String email = "social@test.com";
        String providerId = "12345";

        SocialMemberCheckDto checkDto = new SocialMemberCheckDto(email, providerId, SocialProvider.KAKAO);

        MemberEntity member = MemberEntity.builder()
                .email(email)
                .providerId(providerId)
                .provider(SocialProvider.KAKAO)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.ofNullable(member));

        //when
        boolean result = memberService.isSocialMemberRegistered(checkDto);

        //then
        Assertions.assertTrue(result);

    }

    @Test
    @DisplayName("소셜 사용자의 이메일이 이미 존재하는 경우")
    void isSocialMemberRegisteredEmailDuplicate() {
        //given
        String email = "social@test.com";
        SocialMemberCheckDto checkDto = new SocialMemberCheckDto(
                email, "otherProviderId", SocialProvider.KAKAO);

        MemberEntity member = MemberEntity.builder()
                .email(email)
                .providerId("existingProviderId")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.ofNullable(member));

        //when & then
        GeneralException exception = Assertions.assertThrows(GeneralException.class,
                () -> memberService.isSocialMemberRegistered(checkDto));

        Assertions.assertEquals(ErrorStatus.SOCIAL_EMAIL_DUPLICATE, exception.getCode());
    }


    @Test
    @DisplayName("이메일 중복 발생")
    void emailDuplicateFail() {
        //given
        String email = "test@test.com";
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .build();

        doNothing().when(memberValidatorService).validateEmail(email);
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        //when&then
        GeneralException exception = Assertions.assertThrows(GeneralException.class,
                () -> memberService.validateEmailAndCheckDuplicate(email));

        Assertions.assertEquals(ErrorStatus.EMAIL_DUPLICATE, exception.getCode());

    }


    @Test
    @DisplayName("이메일 중복 발생하지 않음")
    void emailDuplicateSuccess() {
        //given
        String email = "test@test.com";

        doNothing().when(memberValidatorService).validateEmail(email);
        when(memberRepository.existsByEmail(email)).thenReturn(false);

        //when & then
        Assertions.assertDoesNotThrow(() -> memberService.validateEmailAndCheckDuplicate(email));

    }


    @Test
    @DisplayName("닉네임 중복 발생")
    void nameDuplicateFail() {
        //given
        String name = "test";
        MemberEntity member = MemberEntity.builder()
                .name(name)
                .build();

        doNothing().when(memberValidatorService).validateName(name);
        when(memberRepository.existsByName(name)).thenReturn(true);

        //when & then
        GeneralException exception = Assertions.assertThrows(GeneralException.class,
                () -> memberService.validateNameAndCheckDuplicate(name));

        Assertions.assertEquals(ErrorStatus.NAME_DUPLICATE, exception.getCode());
    }


    @Test
    @DisplayName("닉네임 중복 발생하지 않음")
    void nameDuplicateSuccess() {
        //given
        String name = "test";

        doNothing().when(memberValidatorService).validateName(name);
        when(memberRepository.existsByName(name)).thenReturn(false);

        //when & then
        Assertions.assertDoesNotThrow(() -> memberService.validateNameAndCheckDuplicate(name));
    }


    @Test
    @DisplayName("닉네임 변경 성공")
    void updatedNameSuccess() {
        //given
        String email = "test@test.com";
        String newName = "newName";

        MemberEntity member = MemberEntity.builder()
                .email(email)
                .name("oldName")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.ofNullable(member));
        doNothing().when(memberValidatorService).validateName(newName);

        //when
        memberService.updateName(email,newName);

        //then
        Assertions.assertEquals(newName, member.getName());
        verify(memberRepository, times(1)).save(member);
    }


    @Test
    @DisplayName("사용자 선호 카테고리 추가 성공")
    void memberAddCategorySuccess() {
        //given
        String email = "test@test.com";
        MemberCategoryRequest request = new MemberCategoryRequest(List.of(1L, 2L));

        MemberEntity member = MemberEntity.builder()
                .email(email)
                .build();

        CategoryEntity category = CategoryEntity.builder()
                .categoryId(1L)
                .nameEn("Music")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.ofNullable(member));
        when(categoryRepository.findByCategoryId(anyLong())).thenReturn(Optional.ofNullable(category));


        //when
        memberService.memberAddCategory(request,email);

        //then
        verify(memberCategoryRepository, times(request.categories().size())).save(any(MemberCategoryEntity.class));
    }


    @Test
    @DisplayName("선호 카테고리 수정 성공")
    void updateFavoriteCategoriesSuccess() {
        //given
        String email = "test@test.com";
        List<Long> newCategoryIds = List.of(1L, 2L);
        MemberCategoryRequest request = new MemberCategoryRequest(newCategoryIds);

        MemberEntity member = MemberEntity.builder()
                .email(email)
                .build();

        CategoryEntity category = CategoryEntity.builder()
                .categoryId(1L)
                .nameEn("Music")
                .nameKr("음악")
                .build();


        when(categoryRepository.findByCategoryId(anyLong())).thenReturn(Optional.ofNullable(category));
        when(memberRepository.findByEmail(email)).thenReturn(Optional.ofNullable(member));

        //when
        memberService.updateFavoriteCategories(request, email);


        //then
        verify(memberCategoryRepository, times(1))
                .deleteByMemberMemberId(member.getMemberId());

        verify(memberCategoryRepository, times(newCategoryIds.size()))
                .save(any(MemberCategoryEntity.class));

    }











}