package rw.gov.bnr.bnrlicensingportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.AuditLogResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditSeverity;

import java.util.UUID;

/**
 * The only way to write audit records. No update or delete methods — ever.
 */

public interface AuditLogService {

    void log(AuditAction action, User actor, LicenseApplication application,
             String previousState, String newState,
             String ipAddress, String userAgent, AuditSeverity severity);

    void log(AuditAction action, User actor, String ipAddress, String userAgent);

    Page<AuditLogResponse> getLogsForApplication(UUID applicationId, Pageable pageable);

    Page<AuditLogResponse> getAllLogs(Pageable pageable);
}
