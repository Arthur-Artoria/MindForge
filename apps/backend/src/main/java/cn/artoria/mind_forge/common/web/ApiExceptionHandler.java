package cn.artoria.mind_forge.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        ApiErrorResponse response = new ApiErrorResponse(ApiErrorCode.INVALID_REQUEST, "请求参数无效");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
