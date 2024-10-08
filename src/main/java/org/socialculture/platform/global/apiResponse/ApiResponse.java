package org.socialculture.platform.global.apiResponse;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.socialculture.platform.global.apiResponse.success.SuccessStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@Getter
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)//이 설정으로 null값이 들어오면 자동으로 출력에서 제외된다.
    private final T result;

    // 생성자
    private ApiResponse(Boolean isSuccess, HttpStatus httpStatus,String code, String message, T result) {
        this.isSuccess = isSuccess;
        this.httpStatus=httpStatus;
        this.code = code;
        this.message = message;
        this.result = result;
    }



//    public ApiResponse(Boolean isSuccess,HttpStatus code, String  message){
//        this.isSuccess=isSuccess;
//        this.code=code;
//        this.message=message;
//    }




    // 성공한 경우 응답 생성
    public static <T> ResponseEntity<ApiResponse<T>> onSuccess(T result) {
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK,"COMMON200", SuccessStatus._OK.getMessage(), result));
    }

    // 커스텀한 성공시 response 반환값
    public static <T> ResponseEntity<ApiResponse<T>> onSuccess(HttpStatus httpStatus, String code, String message, T result) {
        return new ResponseEntity<>(new ApiResponse<>(true, httpStatus, code, message, result), httpStatus);
    }

    // 반환할 result data가 없는 경우
    public static ResponseEntity<ApiResponse<Void>> onSuccess() {
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK,"COMMON200", SuccessStatus._OK.getMessage(), null));
    }



    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(HttpStatus httpStatus,String code , String message, T data) {
        return new ApiResponse<>(false, httpStatus,code, message, data);
    }

}
/*
* 앞쪽의 <T>는 메서드가 제네릭 타입 T를 사용할 것이라고 선언하는 부분
뒤쪽의 ApiResponse<T>는 반환 타입이 제네릭 타입 T를 포함한 ApiResponse 객체임을 나타냄.
* */
