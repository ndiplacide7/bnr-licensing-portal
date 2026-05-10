package rw.gov.bnr.bnrlicensingportal.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.gov.bnr.bnrlicensingportal.domain.entity.AuditLog;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;

import java.util.UUID;

/**
 * Append-only. Intentionally exposes no deleteBy*, updateBy*, or save(Collection) for
 * modification. Audit records are legal evidence — they are never altered.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByApplicationOrderByCreatedAtDesc(LicenseApplication application, Pageable pageable);

    Page<AuditLog> findByActorOrderByCreatedAtDesc(User actor, Pageable pageable);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
