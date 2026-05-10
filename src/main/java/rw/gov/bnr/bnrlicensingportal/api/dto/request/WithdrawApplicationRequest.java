package rw.gov.bnr.bnrlicensingportal.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawApplicationRequest(
        @NotBlank String reason
) {}
