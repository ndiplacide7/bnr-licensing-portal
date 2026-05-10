package rw.gov.bnr.bnrlicensingportal.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;

// decision must be either APPROVED or REJECTED — validated at service layer
public record DecideApplicationRequest(
        @NotNull ApplicationStatus decision,
        @NotBlank String decisionNotes
) {}
