package rw.gov.bnr.bnrlicensingportal.api.dto.response;

import rw.gov.bnr.bnrlicensingportal.domain.enums.DocumentType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID applicationId,
        DocumentType documentType,
        String originalName,
        String mimeType,
        long sizeBytes,
        String checksumSha256,
        int iteration,
        boolean currentVersion,
        OffsetDateTime createdAt
) {}
