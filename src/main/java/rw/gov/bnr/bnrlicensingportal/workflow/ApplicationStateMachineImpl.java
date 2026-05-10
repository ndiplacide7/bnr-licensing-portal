package rw.gov.bnr.bnrlicensingportal.workflow;

import org.springframework.stereotype.Component;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

import java.util.Arrays;

@Component
public class ApplicationStateMachineImpl implements ApplicationStateMachine {

    @Override
    public void transition(ApplicationStatus from, ApplicationStatus to, Role actorRole) {
        if (!canTransition(from, to, actorRole)) {
            throw new IllegalStateException(
                "Transition %s → %s is not permitted for role %s".formatted(from, to, actorRole)
            );
        }
    }

    @Override
    public boolean canTransition(ApplicationStatus from, ApplicationStatus to, Role actorRole) {
        return Arrays.stream(StatusTransition.values())
                .anyMatch(t -> t.getFrom() == from
                        && t.getTo() == to
                        && t.getAllowedRoles().contains(actorRole));
    }
}
