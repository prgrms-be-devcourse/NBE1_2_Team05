package org.socialculture.platform.ticket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.ticket.dto.response.TicketResponseDto;
import org.socialculture.platform.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.BDDMockito.given;
/**
 * Ticket 컨트롤러 테스트
 *
 * @author ycjung
 */
@WebMvcTest(controllers = TicketController.class)  // TicketController를 대상으로 WebMvcTest를 수행 (컨트롤러 단위 테스트)
@ExtendWith(SpringExtension.class)
@MockBean(JpaMetamodelMappingContext.class)  // JPA 메타모델 문제 해결을 위한 MockBean 설정 (테스트 환경에서 메타모델을 Mock으로 사용)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    @DisplayName("티켓 컨트롤러 조회 테스트")
    @WithMockUser
    void getTickets() throws Exception {
        // 테스트에 사용할 가짜 티켓 응답 데이터를 생성
        List<TicketResponseDto> mockTicketResponsDtos = List.of(
                TicketResponseDto.of(1L, "꿈의 교향곡", LocalDateTime.now(), 2, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)),
                TicketResponseDto.of(2L, "영원의 메아리", LocalDateTime.now(), 3, 150, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2))
        );

        given(ticketService.getAllTicketsByEmailWithPageAndSortOptionDesc(anyInt(), anyInt(), isNull()))
                .willReturn(mockTicketResponsDtos);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "3")
        );

        result.andExpect(status().isOk());
    }
}