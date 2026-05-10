package rw.gov.bnr.bnrlicensingportal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.LoginRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.AuthResponse;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;
import rw.gov.bnr.bnrlicensingportal.security.JwtTokenProvider;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new DisabledException("Account is deactivated");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new LockedException("Account is locked until " + user.getLockedUntil());
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedAttempt(user, ipAddress, userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        auditLogService.log(AuditAction.LOGIN_SUCCESS, user, ipAddress, userAgent);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        UUID userId = jwtTokenProvider.extractUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    @Override
    public void logout(String token) {
        // Stateless logout — the client discards the token.
        // Production implementation would maintain a Redis-based token denylist.
    }

    private void handleFailedAttempt(User user, String ipAddress, String userAgent) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            auditLogService.log(AuditAction.ACCOUNT_LOCKED, user, ipAddress, userAgent);
        } else {
            auditLogService.log(AuditAction.LOGIN_FAILED, user, ipAddress, userAgent);
        }
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        long expiresIn = jwtTokenProvider.getAccessExpirationMs() / 1000;
        return new AuthResponse(accessToken, refreshToken, expiresIn, toUserResponse(user));
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getRole(), u.isActive(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
