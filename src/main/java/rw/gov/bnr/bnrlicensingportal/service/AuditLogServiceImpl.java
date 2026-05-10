package rw.gov.bnr.bnrlicensingportal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.AuditLogResponse;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.AuditLog;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditSeverity;
import rw.gov.bnr.bnrlicensingportal.domain.repository.AuditLogRepository;
import rw.gov.bnr.bnrlicensingportal.domain.repository.LicenseApplicationRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final LicenseApplicationRepository applicationRepository;

    @Override
    @Transactional
    public void log(AuditAction action, User actor, LicenseApplication application,
                    String previousState, String newState,
                    String ipAddress, String userAgent, AuditSeverity severity) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .actor(actor)
                .application(application)
                .previousState(previousState)
                .newState(newState)
                .ipAddress(ipAddress != null ? ipAddress : "SYSTEM")
                .userAgent(userAgent)
                .severity(severity)
                .build());
    }

    @Override
    @Transactional
    public void log(AuditAction action, User actor, String ipAddress, String userAgent) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .actor(actor)
                .ipAddress(ipAddress != null ? ipAddress : "SYSTEM")
                .userAgent(userAgent)
                .severity(AuditSeverity.INFO)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsForApplication(UUID applicationId, Pageable pageable) {
        LicenseApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + applicationId));
        return auditLogRepository
                .findByApplicationOrderByCreatedAtDesc(application, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        User actor = log.getActor();
        return new AuditLogResponse(
                log.getId(),
                log.getApplication() != null ? log.getApplication().getId() : null,
                new UserResponse(actor.getId(), actor.getEmail(), actor.getFirstName(),
                        actor.getLastName(), actor.getRole(), actor.isActive(),
                        actor.getLastLoginAt(), actor.getCreatedAt()),
                log.getAction(),
                log.getPreviousState(),
                log.getNewState(),
                log.getIpAddress(),
                log.getSeverity(),
                log.getCreatedAt()
        );
    }
}
