package rw.gov.bnr.bnrlicensingportal.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.gov.bnr.bnrlicensingportal.domain.enums.LicenseType;

public record CreateApplicationRequest(
        @NotBlank String institutionName,
        @NotNull LicenseType licenseType
) {}
