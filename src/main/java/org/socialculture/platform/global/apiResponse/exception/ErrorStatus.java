package org.socialculture.platform.global.apiResponse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorStatus implements BaseErrorCode{
    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 티켓
    _TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKET404", "해당 티켓을 찾을 수 없습니다."),
    _TICKET_BOOKING_FAILED(HttpStatus.BAD_REQUEST, "TICKET400", "티켓 예약에 실패했습니다."),
    _TICKET_ID_MISSING(HttpStatus.BAD_REQUEST, "TICKET400", "티켓 ID가 제공되지 않았습니다."),
    _TICKET_INVALID_SORT_OPTION(HttpStatus.BAD_REQUEST, "TICKET400", "잘못된 정렬 옵션입니다. 허용된 값은 'ticketId', 'price', 'expired'입니다."),
    _TICKET_INVALID_PAGINATION_PARAMETERS(HttpStatus.BAD_REQUEST, "TICKET400", "페이지나 크기 값이 유효하지 않습니다. 0 이상의 값을 입력해 주세요."),

    // 유저 관련 에러
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "MEMBER4001", "로그인에 실패했습니다."),

    //공연
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFORMANCE404", "공연을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFORMANCE404", "공연 카테고리를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;



    private ErrorStatus(HttpStatus httpStatus, String code, String message){
        this.httpStatus=httpStatus;
        this.code=code;
        this.message=message;
    }

    @Override
    public ErrorResponseDto getResponseWithHttpStatus() {
        return new ErrorResponseDto(httpStatus,false,code,message);
    }//상태코드 실패여부 메세지까지 전달
}
