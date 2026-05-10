package rw.gov.bnr.bnrlicensingportal.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import rw.gov.bnr.bnrlicensingportal.domain.enums.DocumentType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", schema = "bnr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LicenseApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    // BIGINT — semantically correct for byte counts
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(nullable = false)
    @Builder.Default
    private int iteration = 1;

    @Column(name = "is_current_version", nullable = false)
    @Builder.Default
    private boolean currentVersion = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
