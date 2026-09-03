package cn.artoria.mind_forge.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.artoria.mind_forge.common.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthController(AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @Operation(summary = "登录", parameters = { @Parameter(ref = "#/components/parameters/csrfHeader") })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrentUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "CSRF token 无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public CurrentUserResponse login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
                request.password());

        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        // 更新 Session ID
        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return CurrentUserResponse.from((AuthenticatedUser) authentication.getPrincipal());
    }

    @Operation(summary = "获取当前用户", security = {
            @SecurityRequirement(name = "sessionCookie")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取当前用户成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrentUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        var principal = (AuthenticatedUser) authentication.getPrincipal();
        return CurrentUserResponse.from(principal);
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }
}
