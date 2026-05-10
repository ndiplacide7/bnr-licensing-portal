package rw.gov.bnr.bnrlicensingportal.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role,
        @NotBlank String temporaryPassword
) {}
