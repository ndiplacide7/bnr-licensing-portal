package rw.gov.bnr.bnrlicensingportal.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import rw.gov.bnr.bnrlicensingportal.domain.enums.LicenseType;

public record CreateApplicationRequest(
        @NotBlank
        @Pattern(regexp = "\\d{9}", message = "Registration ID must be exactly 9 digits")
        String registrationId,
        @NotBlank String institutionName,
        @NotNull LicenseType licenseType
) {}
