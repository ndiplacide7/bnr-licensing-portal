package rw.gov.bnr.bnrlicensingportal.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperties;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationStateMachineTest {

    private ApplicationStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ApplicationStateMachineImpl();
    }

    @Nested
    @DisplayName("Legal transitions")
    class LegalTransitions {

        @Test
        void applicant_can_submit_draft() {
            assertThat(stateMachine.canTransition(
                    ApplicationStatus.DRAFT, ApplicationStatus.SUBMITTED, Role.APPLICANT
            )).isTrue();
        }

        @Test
        void reviewer_can_start_review() {
            assertThat(stateMachine.canTransition(
                    ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, Role.REVIEWER
            )).isTrue();
        }

        @Test
        void compliance_officer_can_also_start_review() {
            assertThat(stateMachine.canTransition(
                    ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, Role.COMPLIANCE_OFFICER
            )).isTrue();
        }

        @Test
        void approver_can_approve_completed_review() {
            assertThat(stateMachine.canTransition(
                    ApplicationStatus.REVIEW_COMPLETED, ApplicationStatus.APPROVED, Role.APPROVER
            )).isTrue();
        }

        @Test
        void approver_can_reject_completed_review() {
            assertThat(stateMachine.canTransition(
                    ApplicationStatus.REVIEW_COMPLETED, ApplicationStatus.REJECTED, Role.APPROVER
            )).isTrue();
        }
    }

    @Nested
    @DisplayName("Illegal transitions")
    class IllegalTransitions {

        @Test
        void applicant_cannot_approve() {
            assertThatThrownBy(() -> stateMachine.transition(
                    ApplicationStatus.REVIEW_COMPLETED, ApplicationStatus.APPROVED, Role.APPLICANT
            )).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void reviewer_cannot_skip_directly_to_approved() {
            assertThatThrownBy(() -> stateMachine.transition(
                    ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED, Role.REVIEWER
            )).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void reviewer_cannot_submit_on_behalf_of_applicant() {
            assertThatThrownBy(() -> stateMachine.transition(
                    ApplicationStatus.DRAFT, ApplicationStatus.SUBMITTED, Role.REVIEWER
            )).isInstanceOf(IllegalStateException.class);
        }
    }
}
