package rw.gov.bnr.bnrlicensingportal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;
import rw.gov.bnr.bnrlicensingportal.domain.enums.LicenseType;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;
import rw.gov.bnr.bnrlicensingportal.domain.repository.LicenseApplicationRepository;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;

import java.time.OffsetDateTime;

/**
 * Seeds one user per role and two applications in different states on first startup.
 * All seed users share the password: Password1!
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final LicenseApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with initial users and applications...");
        String hash = passwordEncoder.encode("Password1!");

        User applicant  = save(user("applicant@bnr.rw",  "Mugisha",  "Derick",    Role.APPLICANT,          hash));
        User reviewer   = save(user("reviewer@bnr.rw",   "Nkusi",    "Assouman",  Role.REVIEWER,           hash));
                          save(user("approver@bnr.rw",   "Habimana", "Calixte",   Role.APPROVER,           hash));
                          save(user("compliance@bnr.rw", "Uwase",    "Jannette",  Role.COMPLIANCE_OFFICER, hash));
                          save(user("auditor@bnr.rw",    "Karangwa", "Richard",   Role.AUDITOR,            hash));
                          save(user("admin@bnr.rw",      "Placido",  "Placide",   Role.SYSTEM_ADMIN,       hash));

        // Application 1 — DRAFT: created by applicant, not yet submitted
        applicationRepository.save(LicenseApplication.builder()
                .registrationId("123456789")
                .institutionName("Kigali Microfinance Ltd")
                .licenseType(LicenseType.MICROFINANCE_INSTITUTION)
                .applicant(applicant)
                .status(ApplicationStatus.DRAFT)
                .build());

        // Application 2 — UNDER_REVIEW: submitted 3 days ago, assigned to reviewer
        applicationRepository.save(LicenseApplication.builder()
                .registrationId("987654321")
                .institutionName("Rwanda Commercial Bank")
                .licenseType(LicenseType.COMMERCIAL_BANK)
                .applicant(applicant)
                .reviewer(reviewer)
                .status(ApplicationStatus.UNDER_REVIEW)
                .submittedAt(OffsetDateTime.now().minusDays(3))
                .build());

        log.info("Seeding complete. Login with any seeded user — password: Password1!");
        log.info("  SYSTEM_ADMIN      : admin@bnr.rw");
        log.info("  APPLICANT         : applicant@bnr.rw");
        log.info("  REVIEWER          : reviewer@bnr.rw");
        log.info("  APPROVER          : approver@bnr.rw");
        log.info("  COMPLIANCE_OFFICER: compliance@bnr.rw");
        log.info("  AUDITOR           : auditor@bnr.rw");
    }

    private User user(String email, String lastName, String firstName, Role role, String hash) {
        return User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .passwordHash(hash)
                .build();
    }

    private User save(User u) {
        return userRepository.save(u);
    }
}
