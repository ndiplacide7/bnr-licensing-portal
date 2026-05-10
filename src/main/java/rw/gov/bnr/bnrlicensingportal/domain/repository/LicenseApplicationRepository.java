package rw.gov.bnr.bnrlicensingportal.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.ApplicationStatus;

import java.util.UUID;

@Repository
public interface LicenseApplicationRepository extends JpaRepository<LicenseApplication, UUID> {
    Page<LicenseApplication> findByApplicant(User applicant, Pageable pageable);
    Page<LicenseApplication> findByReviewer(User reviewer, Pageable pageable);
    Page<LicenseApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
