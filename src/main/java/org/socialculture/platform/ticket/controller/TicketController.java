package org.socialculture.platform.ticket.controller;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialculture.platform.global.apiResponse.ApiResponse;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.ticket.dto.response.TicketResponseDto;
import org.socialculture.platform.ticket.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 티켓 데이터 컨트롤러
 *
 * @author ycjung
 */
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    /**
     * 나의 티켓 전체 조회 - 페이징 처리
     * @param page
     * @param size
     * @param option
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponseDto>>> getAllTicketsByMemberIdWithPageAndSortOptionDesc(
            @RequestParam("page") int page, @RequestParam("size") int size, @Nullable @RequestParam("option") String option) {
        logger.info("Get tickets by member id with pagination and sort - Page: {}, Size: {}, Sort Option: {}", page, size, option);

        return ApiResponse.onSuccess(ticketService.getAllTicketsByEmailWithPageAndSortOptionDesc(page, size, option));
    }

    /**
     * 나의 티켓 상세 조회
     * @param ticketId
     * @return
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponseDto>> getTicketById(@PathVariable("ticketId") Long ticketId) {
        logger.info("Get ticket detail by id: {}", ticketId);

        if (ticketId == null) {
            throw new GeneralException(ErrorStatus._TICKET_ID_MISSING);
        }
        return ApiResponse.onSuccess(ticketService.getTicketByEmailAndTicketId(ticketId));
    }
}
