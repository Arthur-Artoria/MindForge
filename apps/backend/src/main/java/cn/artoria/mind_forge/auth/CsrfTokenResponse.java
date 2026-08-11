package cn.artoria.mind_forge.auth;

public record CsrfTokenResponse(
        String headerName,
        String token) {
}
