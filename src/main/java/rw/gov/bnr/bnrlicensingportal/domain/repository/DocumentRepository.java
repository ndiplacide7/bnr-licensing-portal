package rw.gov.bnr.bnrlicensingportal.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.gov.bnr.bnrlicensingportal.domain.entity.Document;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByApplicationAndCurrentVersionTrue(LicenseApplication application);

    List<Document> findByApplicationOrderByIterationDesc(LicenseApplication application);

    @Modifying
    @Query("UPDATE Document d SET d.currentVersion = false WHERE d.application = :application")
    void markAllAsNotCurrentVersion(LicenseApplication application);
}
