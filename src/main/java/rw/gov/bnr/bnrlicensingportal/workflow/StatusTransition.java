package rw.gov.bnr.bnrlicensingportal.workflow;

import lombok.Getter;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

import java.util.Set;

import static rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus.*;
import static rw.gov.bnr.bnrlicensingportal.domain.enums.Role.*;

/**
 * Defines every legal transition: from-state → to-state, and which roles may trigger it.
 * Adding a new transition here is the only change needed to extend the workflow.
 */
@Getter
public enum StatusTransition {

    SUBMIT            (DRAFT,            SUBMITTED,        Set.of(APPLICANT)),
    ASSIGN_REVIEW     (SUBMITTED,        UNDER_REVIEW,     Set.of(REVIEWER, COMPLIANCE_OFFICER)),
    COMPLETE_REVIEW   (UNDER_REVIEW,     REVIEW_COMPLETED, Set.of(REVIEWER)),
    APPROVE           (REVIEW_COMPLETED, APPROVED,         Set.of(APPROVER)),
    REJECT            (REVIEW_COMPLETED, REJECTED,         Set.of(APPROVER)),
    WITHDRAW_DRAFT    (DRAFT,            WITHDRAWN,        Set.of(APPLICANT)),
    WITHDRAW_SUBMITTED(SUBMITTED,        WITHDRAWN,        Set.of(APPLICANT)),
    WITHDRAW_REVIEW   (UNDER_REVIEW,     WITHDRAWN,        Set.of(APPLICANT));

    private final ApplicationStatus from;
    private final ApplicationStatus to;
    private final Set<Role> allowedRoles;

    StatusTransition(ApplicationStatus from, ApplicationStatus to, Set<Role> allowedRoles) {
        this.from = from;
        this.to = to;
        this.allowedRoles = allowedRoles;
    }
}
