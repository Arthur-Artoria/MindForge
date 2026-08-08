package cn.artoria.mind_forge.auth;

public record CurrentUserResponse(
        Long id,
        String email,
        String username) {
    public static CurrentUserResponse from(AuthenticatedUser principal) {
        return new CurrentUserResponse(
                principal.id(),
                principal.email(),
                principal.displayName());
    }
}
