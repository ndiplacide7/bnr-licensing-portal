package rw.gov.bnr.bnrlicensingportal.service;

import rw.gov.bnr.bnrlicensingportal.api.dto.request.LoginRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);
    AuthResponse refresh(String refreshToken);
    void logout(String token, String ipAddress, String userAgent);
}
