package rw.gov.bnr.bnrlicensingportal.workflow;

import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

/**
 * Validates and enforces legal status transitions for a LicenseApplication.
 * Every status change must pass through this contract — nothing bypasses it.
 */
public interface ApplicationStateMachine {

    void transition(ApplicationStatus from, ApplicationStatus to, Role actorRole);

    boolean canTransition(ApplicationStatus from, ApplicationStatus to, Role actorRole);
}
