package org.socialculture.platform.global.apiResponse;

public interface BaseErrorCode {
     ErrorResponseDto getResponseDetails();//메서지, 코드, 결과
     ErrorResponseDto getResponseWithHttpStatus();//메세지 ,코드 ,결과, http상태코드까지
}
