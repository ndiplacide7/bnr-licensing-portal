package rw.gov.bnr.bnrlicensingportal.api.dto.response;

import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean active,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt
) {}
