package org.socialculture.platform.performance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.socialculture.platform.performance.dto.CategoryDto;
import org.socialculture.platform.performance.dto.request.PerformanceRegisterRequest;
import org.socialculture.platform.performance.dto.request.PerformanceUpdateRequest;
import org.socialculture.platform.performance.dto.response.PerformanceDetailResponse;
import org.socialculture.platform.performance.dto.response.PerformanceListResponse;
import org.socialculture.platform.performance.dto.response.PerformanceRegisterResponse;
import org.socialculture.platform.performance.dto.response.PerformanceUpdateResponse;
import org.socialculture.platform.performance.entity.PerformanceStatus;
import org.socialculture.platform.performance.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.socialculture.platform.util.ApiDocumentUtils.getDocumentRequest;
import static org.socialculture.platform.util.ApiDocumentUtils.getDocumentResponse;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "docs.api.com")
public class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PerformanceService performanceService;

    private final String email = "test@test.com";

    @Test
    @DisplayName("공연 등록 조회 테스트")
    @WithMockUser
    void getPerformanceRegisterTest() throws Exception {
        //given
        PerformanceRegisterRequest performanceRegisterRequest =
                PerformanceRegisterRequest.builder()
                        .title("제목111")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(4L))
                        .address("주소111111")
                        .imageUrl("이미지 주소")
                        .price(10000)
                        .description("설명입니다.")
                        .maxAudience(50)
                        .startDate(LocalDateTime.now().minusDays(2))
                        .categories(List.of("노래", "스탠드업"))
                        .build();

        PerformanceRegisterResponse performanceRegisterResponse =
                PerformanceRegisterResponse.builder()
                        .memberName("홍길동")
                        .performanceId(1L)
                        .title("제목111")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(4L))
                        .description("설명입니다.")
                        .maxAudience(50)
                        .address("주소111111")
                        .imageUrl("이미지 주소")
                        .price(10000)
                        .remainingTickets(50)
                        .startDate(LocalDateTime.now().minusDays(2))
                        .status(PerformanceStatus.NOT_CONFIRMED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(4L, "스탠드업", "STANDUP")
                                )
                        )
                        .build();

        given(performanceService.registerPerformance(email, performanceRegisterRequest, any(MultipartFile.class)))
                .willReturn(performanceRegisterResponse);

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/performances")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performanceRegisterRequest))
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-register",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("공연 제목"),
                                fieldWithPath("dateStartTime").type(JsonFieldType.STRING).description("공연 시작시간"),
                                fieldWithPath("dateEndTime").type(JsonFieldType.STRING).description("공연 종료시간"),
                                fieldWithPath("address").type(JsonFieldType.STRING).description("공연장 주소"),
                                fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("공연 이미지 URL"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("description").type(JsonFieldType.STRING).description("공연 상세설명"),
                                fieldWithPath("maxAudience").type(JsonFieldType.NUMBER).description("공연 총 좌석수"),
                                fieldWithPath("startDate").type(JsonFieldType.STRING).description("판매 시작일"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("공연 카테고리 배열(한국어만)")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태"),
                                fieldWithPath("result.memberName").type(JsonFieldType.STRING).description("주최자 이름"),
                                fieldWithPath("result.performanceId").type(JsonFieldType.NUMBER).description("공연 ID"),
                                fieldWithPath("result.title").type(JsonFieldType.STRING).description("공연 제목"),
                                fieldWithPath("result.dateStartTime").type(JsonFieldType.STRING).description("공연 시작시간"),
                                fieldWithPath("result.dateEndTime").type(JsonFieldType.STRING).description("공연 종료시간"),
                                fieldWithPath("result.description").type(JsonFieldType.STRING).description("공연 상세설명"),
                                fieldWithPath("result.maxAudience").type(JsonFieldType.NUMBER).description("공연 총 좌석수"),
                                fieldWithPath("result.address").type(JsonFieldType.STRING).description("공연장 주소"),
                                fieldWithPath("result.imageUrl").type(JsonFieldType.STRING).description("공연 이미지 URL"),
                                fieldWithPath("result.price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("result.remainingTickets").type(JsonFieldType.NUMBER).description("남은 티켓 갯수"),
                                fieldWithPath("result.startDate").type(JsonFieldType.STRING).description("판매 시작일"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("공연 상태"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("게시글 생성 시간"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("게시글 최종 수정 시간"),
                                fieldWithPath("result.categories[]").description("공연 카테고리 배열"),
                                fieldWithPath("result.categories[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("result.categories[].nameKr").type(JsonFieldType.STRING).description("카테고리 한국어"),
                                fieldWithPath("result.categories[].nameEn").type(JsonFieldType.STRING).description("카테고리 영어")
                        )
                ));
    }

    @Test
    @DisplayName("공연 전체 조회 테스트")
    @WithMockUser
    void getPerformanceListTest() throws Exception {
        //given
        List<PerformanceListResponse> performanceList = List.of(
                PerformanceListResponse.builder()
                        .memberName("홍길동")
                        .performanceId(1L)
                        .title("제목111")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(4L))
                        .address("주소111111")
                        .imageUrl("이미지 주소")
                        .price(10000)
                        .status("NOT_CONFIRMED")
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(4L, "스탠드업", "STANDUP")
                                )
                        )
                        .build()
                ,
                PerformanceListResponse.builder()
                        .memberName("홍길동222")
                        .performanceId(1L)
                        .title("제목122211")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(5L))
                        .address("주소114231111")
                        .imageUrl("이미지 주소")
                        .price(20000)
                        .status("CONFIRMED")
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(2L, "춤", "DANCE")
                                )
                        )
                        .build()
        );

        given(performanceService.getPerformanceList(anyInt(), anyInt(), anyLong(), anyString()))
                .willReturn(performanceList);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "3")
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-list",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태"),
                                fieldWithPath("result[].memberName").type(JsonFieldType.STRING).description("주최자 이름"),
                                fieldWithPath("result[].performanceId").type(JsonFieldType.NUMBER).description("공연 ID"),
                                fieldWithPath("result[].title").type(JsonFieldType.STRING).description("공연 제목"),
                                fieldWithPath("result[].dateStartTime").type(JsonFieldType.STRING).description("공연 시작시간"),
                                fieldWithPath("result[].dateEndTime").type(JsonFieldType.STRING).description("공연 종료시간"),
                                fieldWithPath("result[].address").type(JsonFieldType.STRING).description("공연장 주소"),
                                fieldWithPath("result[].imageUrl").type(JsonFieldType.STRING).description("공연 이미지 URL"),
                                fieldWithPath("result[].price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("result[].status").type(JsonFieldType.STRING).description("공연 상태"),
                                fieldWithPath("result[].categories[]").description("공연 카테고리 배열"),
                                fieldWithPath("result[].categories[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("result[].categories[].nameKr").type(JsonFieldType.STRING).description("카테고리 한국어"),
                                fieldWithPath("result[].categories[].nameEn").type(JsonFieldType.STRING).description("카테고리 영어")
                        )
                ));
    }

    @Test
    @DisplayName("공연 상세 조회 테스트")
    @WithMockUser
    void getPerformanceDetailTest() throws Exception {
        //given
        PerformanceDetailResponse performanceDetailResponse =
                PerformanceDetailResponse.builder()
                        .memberName("홍길동")
                        .performanceId(1L)
                        .title("제목111")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(4L))
                        .description("설명입니다.")
                        .maxAudience(50)
                        .address("주소111111")
                        .imageUrl("이미지 주소")
                        .price(10000)
                        .remainingTickets(50)
                        .startDate(LocalDateTime.now().minusDays(2))
                        .status(PerformanceStatus.NOT_CONFIRMED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(4L, "스탠드업", "STANDUP")
                                )
                        )
                        .build();

        given(performanceService.getPerformanceDetail(email, anyLong()))
                .willReturn(performanceDetailResponse);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/performances/1")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-detail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태"),
                                fieldWithPath("result.memberName").type(JsonFieldType.STRING).description("주최자 이름"),
                                fieldWithPath("result.performanceId").type(JsonFieldType.NUMBER).description("공연 ID"),
                                fieldWithPath("result.title").type(JsonFieldType.STRING).description("공연 제목"),
                                fieldWithPath("result.dateStartTime").type(JsonFieldType.STRING).description("공연 시작시간"),
                                fieldWithPath("result.dateEndTime").type(JsonFieldType.STRING).description("공연 종료시간"),
                                fieldWithPath("result.description").type(JsonFieldType.STRING).description("공연 상세설명"),
                                fieldWithPath("result.maxAudience").type(JsonFieldType.NUMBER).description("공연 총 좌석수"),
                                fieldWithPath("result.address").type(JsonFieldType.STRING).description("공연장 주소"),
                                fieldWithPath("result.imageUrl").type(JsonFieldType.STRING).description("공연 이미지 URL"),
                                fieldWithPath("result.price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("result.remainingTickets").type(JsonFieldType.NUMBER).description("남은 티켓 갯수"),
                                fieldWithPath("result.startDate").type(JsonFieldType.STRING).description("판매 시작일"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("공연 상태"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("게시글 생성 시간"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("게시글 최종 수정 시간"),
                                fieldWithPath("result.categories[]").description("공연 카테고리 배열"),
                                fieldWithPath("result.categories[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("result.categories[].nameKr").type(JsonFieldType.STRING).description("카테고리 한국어"),
                                fieldWithPath("result.categories[].nameEn").type(JsonFieldType.STRING).description("카테고리 영어")
                        )
                ));
    }


    @Test
    @DisplayName("공연 수정 테스트")
    @WithMockUser
    void performanceUpdateTest() throws Exception {
        //given
        PerformanceUpdateRequest performanceUpdateRequest =
                PerformanceUpdateRequest.builder()
                        .title("수정제목 11")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now())
                        .description("설명 수정합니다.")
                        .price(5000)
                        .build();
        PerformanceUpdateResponse performanceUpdateResponse = PerformanceUpdateResponse.from(1L);

        given(performanceService.updatePerformance(anyString(), anyLong(), any()))
                .willReturn(performanceUpdateResponse);

        ResultActions result = this.mockMvc.perform(
                patch("/api/v1/performances/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(performanceUpdateRequest))
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-update",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).optional().description("공연 제목"),
                                fieldWithPath("dateStartTime").type(JsonFieldType.STRING).optional().description("공연 시작시간"),
                                fieldWithPath("dateEndTime").type(JsonFieldType.STRING).optional().description("공연 종료시간"),
                                fieldWithPath("address").type(JsonFieldType.STRING).optional().description("공연장 주소"),
                                fieldWithPath("imageUrl").type(JsonFieldType.STRING).optional().description("공연 이미지 URL"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).optional().description("티켓 가격"),
                                fieldWithPath("description").type(JsonFieldType.STRING).optional().description("공연 상세설명"),
                                fieldWithPath("maxAudience").type(JsonFieldType.NUMBER).optional().description("공연 총 좌석수")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태"),
                                fieldWithPath("result.performanceId").type(JsonFieldType.NUMBER).description("공연 ID")
                        )
                ));
    }

    @Test
    @DisplayName("공연 삭제 테스트")
    @WithMockUser
    void deletePerformanceTest() throws Exception {
        //given
        doNothing().when(performanceService).deletePerformance(anyString(), anyLong());


        ResultActions result = this.mockMvc.perform(
                delete("/api/v1/performances/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-detail",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태")
                        )
                ));
    }

    @Test
    @DisplayName("내가 등록한 공연 조회 테스트")
    @WithMockUser
    void getMyPerformanceListTest() throws Exception {
        //given
        List<PerformanceListResponse> performanceList = List.of(
                PerformanceListResponse.builder()
                        .memberName("홍길동")
                        .performanceId(1L)
                        .title("제목111")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(4L))
                        .address("주소111111")
                        .imageUrl("이미지 주소")
                        .price(10000)
                        .status("NOT_CONFIRMED")
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(4L, "스탠드업", "STANDUP")
                                )
                        )
                        .build()
                ,
                PerformanceListResponse.builder()
                        .memberName("홍길동")
                        .performanceId(1L)
                        .title("제목122211")
                        .dateStartTime(LocalDateTime.now())
                        .dateEndTime(LocalDateTime.now().plusHours(5L))
                        .address("주소114231111")
                        .imageUrl("이미지 주소")
                        .price(20000)
                        .status("CONFIRMED")
                        .categories(
                                List.of(
                                        CategoryDto.of(1L, "노래", "MUSIC"),
                                        CategoryDto.of(2L, "춤", "DANCE")
                                )
                        )
                        .build()
        );

        given(performanceService.getMyPerformanceList(anyString(), anyInt(), anyInt()))
                .willReturn(performanceList);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/performances/admin/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "3")
        );

        result.andExpect(status().isOk())
                .andDo(document("performance-list",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("성공 여부"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("httpStatus").description("HTTP 상태"),
                                fieldWithPath("result[].memberName").type(JsonFieldType.STRING).description("주최자 이름"),
                                fieldWithPath("result[].performanceId").type(JsonFieldType.NUMBER).description("공연 ID"),
                                fieldWithPath("result[].title").type(JsonFieldType.STRING).description("공연 제목"),
                                fieldWithPath("result[].dateStartTime").type(JsonFieldType.STRING).description("공연 시작시간"),
                                fieldWithPath("result[].dateEndTime").type(JsonFieldType.STRING).description("공연 종료시간"),
                                fieldWithPath("result[].address").type(JsonFieldType.STRING).description("공연장 주소"),
                                fieldWithPath("result[].imageUrl").type(JsonFieldType.STRING).description("공연 이미지 URL"),
                                fieldWithPath("result[].price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("result[].status").type(JsonFieldType.STRING).description("공연 상태"),
                                fieldWithPath("result[].categories[]").description("공연 카테고리 배열"),
                                fieldWithPath("result[].categories[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("result[].categories[].nameKr").type(JsonFieldType.STRING).description("카테고리 한국어"),
                                fieldWithPath("result[].categories[].nameEn").type(JsonFieldType.STRING).description("카테고리 영어")
                        )
                ));
    }
}
