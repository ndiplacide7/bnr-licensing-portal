package rw.gov.bnr.bnrlicensingportal.api.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponse user
) {}
