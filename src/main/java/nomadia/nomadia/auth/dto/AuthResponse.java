package nomadia.nomadia.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInMs) {
}
