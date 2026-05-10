package rw.gov.bnr.bnrlicensingportal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.DecideApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.ApplicationResponse;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.*;
import rw.gov.bnr.bnrlicensingportal.domain.repository.LicenseApplicationRepository;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;
import rw.gov.bnr.bnrlicensingportal.security.RequestContext;
import rw.gov.bnr.bnrlicensingportal.workflow.ApplicationStateMachine;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final LicenseApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationStateMachine stateMachine;
    private final AuditLogService auditLogService;
    private final RequestContext requestContext;

    @Override
    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request, UUID applicantId) {
        User applicant = findUser(applicantId);
        LicenseApplication app = applicationRepository.save(LicenseApplication.builder()
                .applicant(applicant)
                .institutionName(request.institutionName())
                .licenseType(request.licenseType())
                .status(ApplicationStatus.DRAFT)
                .build());

        auditLogService.log(AuditAction.APPLICATION_CREATED, applicant, app,
                null, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);

        return toResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getById(UUID id, UUID actorId) {
        LicenseApplication app = findApplication(id);
        User actor = findUser(actorId);
        assertCanView(app, actor);
        return toResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listForActor(UUID actorId, Pageable pageable) {
        User actor = findUser(actorId);
        return switch (actor.getRole()) {
            case APPLICANT -> applicationRepository.findByApplicant(actor, pageable).map(this::toResponse);
            case REVIEWER  -> applicationRepository.findByReviewer(actor, pageable).map(this::toResponse);
            default        -> applicationRepository.findAll(pageable).map(this::toResponse);
        };
    }

    @Override
    @Transactional
    public ApplicationResponse submit(UUID applicationId, UUID actorId) {
        User actor = findUser(actorId);
        LicenseApplication app = findApplication(applicationId);
        assertOwner(app, actor);

        String prev = snapshot(app);
        stateMachine.transition(app.getStatus(), ApplicationStatus.SUBMITTED, actor.getRole());
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setSubmittedAt(OffsetDateTime.now());
        applicationRepository.save(app);

        auditLogService.log(AuditAction.APPLICATION_SUBMITTED, actor, app,
                prev, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);
        return toResponse(app);
    }

    @Override
    @Transactional
    public ApplicationResponse assignReviewer(UUID applicationId, UUID reviewerId, UUID actorId) {
        User actor    = findUser(actorId);
        User reviewer = findUser(reviewerId);
        LicenseApplication app = findApplication(applicationId);

        String prev = snapshot(app);
        stateMachine.transition(app.getStatus(), ApplicationStatus.UNDER_REVIEW, actor.getRole());
        app.setReviewer(reviewer);
        app.setStatus(ApplicationStatus.UNDER_REVIEW);
        applicationRepository.save(app);

        auditLogService.log(AuditAction.APPLICATION_ASSIGNED, actor, app,
                prev, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);
        return toResponse(app);
    }

    @Override
    @Transactional
    public ApplicationResponse completeReview(UUID applicationId, UUID actorId) {
        User actor = findUser(actorId);
        LicenseApplication app = findApplication(applicationId);
        assertAssignedReviewer(app, actor);

        String prev = snapshot(app);
        stateMachine.transition(app.getStatus(), ApplicationStatus.REVIEW_COMPLETED, actor.getRole());
        app.setStatus(ApplicationStatus.REVIEW_COMPLETED);
        app.setReviewedAt(OffsetDateTime.now());
        applicationRepository.save(app);

        auditLogService.log(AuditAction.REVIEW_COMPLETED, actor, app,
                prev, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);
        return toResponse(app);
    }

    @Override
    @Transactional
    public ApplicationResponse decide(UUID applicationId, DecideApplicationRequest request, UUID actorId) {
        ApplicationStatus decision = request.decision();
        if (decision != ApplicationStatus.APPROVED && decision != ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }

        User actor = findUser(actorId);
        LicenseApplication app = findApplication(applicationId);

        // Hard rule: the reviewer of an application cannot make the final approval decision
        if (app.getReviewer() != null && app.getReviewer().getId().equals(actor.getId())) {
            throw new IllegalStateException(
                    "The reviewer of an application cannot make the final approval decision on it");
        }

        String prev = snapshot(app);
        stateMachine.transition(app.getStatus(), decision, actor.getRole());
        app.setStatus(decision);
        app.setApprover(actor);
        app.setDecisionNotes(request.decisionNotes());
        app.setDecidedAt(OffsetDateTime.now());
        applicationRepository.save(app);

        AuditAction action = (decision == ApplicationStatus.APPROVED)
                ? AuditAction.APPLICATION_APPROVED : AuditAction.APPLICATION_REJECTED;
        auditLogService.log(action, actor, app,
                prev, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);
        return toResponse(app);
    }

    @Override
    @Transactional
    public ApplicationResponse withdraw(UUID applicationId, String reason, UUID actorId) {
        User actor = findUser(actorId);
        LicenseApplication app = findApplication(applicationId);
        assertOwner(app, actor);

        String prev = snapshot(app);
        stateMachine.transition(app.getStatus(), ApplicationStatus.WITHDRAWN, actor.getRole());
        app.setStatus(ApplicationStatus.WITHDRAWN);
        app.setWithdrawalReason(reason);
        applicationRepository.save(app);

        auditLogService.log(AuditAction.APPLICATION_WITHDRAWN, actor, app,
                prev, snapshot(app), requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.WARNING);
        return toResponse(app);
    }

    private void assertOwner(LicenseApplication app, User actor) {
        if (!app.getApplicant().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the applicant can perform this action");
        }
    }

    private void assertAssignedReviewer(LicenseApplication app, User actor) {
        if (app.getReviewer() == null || !app.getReviewer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the assigned reviewer can complete the review");
        }
    }

    private void assertCanView(LicenseApplication app, User actor) {
        if (actor.getRole() == Role.APPLICANT && !app.getApplicant().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private LicenseApplication findApplication(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private String snapshot(LicenseApplication app) {
        return """
                {"id":"%s","status":"%s","institutionName":"%s","licenseType":"%s"}""".formatted(
                app.getId(), app.getStatus(), app.getInstitutionName(), app.getLicenseType());
    }

    private ApplicationResponse toResponse(LicenseApplication app) {
        return new ApplicationResponse(
                app.getId(),
                app.getInstitutionName(),
                app.getLicenseType(),
                app.getStatus(),
                toUserResponse(app.getApplicant()),
                app.getReviewer()  != null ? toUserResponse(app.getReviewer())  : null,
                app.getApprover()  != null ? toUserResponse(app.getApprover())  : null,
                app.getDecisionNotes(),
                app.getWithdrawalReason(),
                app.getSubmittedAt(),
                app.getReviewedAt(),
                app.getDecidedAt(),
                app.getVersion(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getRole(), u.isActive(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
