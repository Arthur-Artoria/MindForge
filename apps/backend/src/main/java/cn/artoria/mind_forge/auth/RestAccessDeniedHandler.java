package cn.artoria.mind_forge.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

import cn.artoria.mind_forge.common.web.ApiErrorCode;
import cn.artoria.mind_forge.common.web.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiErrorCode errorCode;
        String errorMessage;
        if (accessDeniedException instanceof CsrfException) {
            errorCode = ApiErrorCode.CSRF_TOKEN_INVALID;
            errorMessage = "CSRF token 缺失或无效";
        } else {
            errorCode = ApiErrorCode.ACCESS_DENIED;
            errorMessage = "访问被拒绝";
        }
        
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode, errorMessage);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), apiErrorResponse);
    }

}
