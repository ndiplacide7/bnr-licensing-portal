package rw.gov.bnr.bnrlicensingportal.api.dto.response;

import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.LicenseType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        String institutionName,
        LicenseType licenseType,
        ApplicationStatus status,
        UserResponse applicant,
        UserResponse reviewer,
        UserResponse approver,
        String decisionNotes,
        String withdrawalReason,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        OffsetDateTime decidedAt,
        int version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
