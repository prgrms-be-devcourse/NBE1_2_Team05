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

    // 쿠폰
    _COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON400", "해당 쿠폰을 찾을 수 없습니다."),
    _COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUPON400", "이미 사용된 쿠폰입니다."),
    _COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON400", "만료된 쿠폰입니다."),

    // 유저 관련 에러
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "MEMBER4001", "로그인에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER401", "유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요."),
    REGISTER_FAIL(HttpStatus.UNAUTHORIZED, "MEMBER401", "회원가입에 실패했습니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER409", "이미 사용중인 이메일 입니다."),
    EMAIL_INVALID(HttpStatus.BAD_REQUEST, "MEMBER400", "이메일 형식이 맞지 않습니다."),
    NAME_DUPLICATE(HttpStatus.CONFLICT, "MEMBER409", "이미 사용중인 닉네임 입니다."),
    NAME_INVALID(HttpStatus.BAD_REQUEST, "MEMBER400", "닉네임 형식이 올바르지 않습니다."),
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "MEMBER400", "비밀번호 형식이 맞지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "해당 유저가 존재하지 않습니다."),


    // 소셜 유저 관련 에러
    SOCIAL_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "SOCIAL409", "이메일이 이미 다른 소셜 계정에서 사용중입니다."),
    SOCIAL_NAME_INVALID(HttpStatus.BAD_REQUEST, "SOCIAL400", "닉네임 형식이 올바르지 않습니다."),
    SOCIAL_NAME_DUPLICATE(HttpStatus.CONFLICT, "SOCIAL409", "닉네임이 이미 사용 중입니다."),
    SOCIAL_NAME_REQUIRED(HttpStatus.FOUND, "SOCIAL302", "회원가입을 위해 닉네임이 필요합니다."),
    SOCIAL_INFO_INVALID(HttpStatus.BAD_REQUEST, "SOCIAL400", "사용자의 기본정보가 적절하지 않습니다."),


    // 멤버 카테고리 관련
    MEMBER_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY404", "해당 카테고리가 존재하지 않습니다."),


    //공연
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFORMANCE404", "공연을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PERFORMANCE404", "공연 카테고리를 찾을 수 없습니다."),

    //댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT404","댓글을 찾을 수 없습니다."),
    _COMMENT_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "COMMENT403", "댓글 수정 권한이 없습니다. 본인이 작성한 댓글만 수정할 수 있습니다.");


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
