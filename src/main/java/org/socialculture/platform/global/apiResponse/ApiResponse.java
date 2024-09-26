package org.socialculture.platform.global.apiResponse;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final HttpStatus code;
    private  String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  T result;

    // 생성자
    public ApiResponse(Boolean isSuccess, HttpStatus code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public ApiResponse(Boolean isSuccess,HttpStatus code, String  message){
        this.isSuccess=isSuccess;
        this.code=code;
        this.message=message;
    }


    // Getter 메서드
    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public HttpStatus getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>(true, HttpStatus.OK, SuccessStatus._OK.getMessage(), result);
    }//고정된 성공시 response반환값

    public static <T> ApiResponse<T> onSuccess(HttpStatus code, String message,T result) {
        return new ApiResponse<>(true, code, message, result);
    }//커스텀한 성공시 response반환값

    //result 반환값이 없는경우도 만들어야함

    public static  ApiResponse<Void> onSuccess(){
        return new ApiResponse<>(true,HttpStatus.OK,SuccessStatus._OK.getMessage(),null);
    }//반환할 result data가 없는경우

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(HttpStatus code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

}
