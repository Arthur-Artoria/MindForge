package cn.artoria.mind_forge.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.artoria.mind_forge.common.web.ApiErrorCode;
import cn.artoria.mind_forge.common.web.ApiErrorResponse;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailure() {
        ApiErrorResponse errorResponse = new ApiErrorResponse(ApiErrorCode.INVALID_CREDENTIALS, "无效的邮箱或密码");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}
