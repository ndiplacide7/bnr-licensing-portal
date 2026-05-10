package rw.gov.bnr.bnrlicensingportal.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.DocumentResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.Document;
import rw.gov.bnr.bnrlicensingportal.domain.entity.LicenseApplication;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditSeverity;
import rw.gov.bnr.bnrlicensingportal.domain.enums.DocumentType;
import rw.gov.bnr.bnrlicensingportal.domain.repository.DocumentRepository;
import rw.gov.bnr.bnrlicensingportal.domain.repository.LicenseApplicationRepository;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;
import rw.gov.bnr.bnrlicensingportal.security.RequestContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private final DocumentRepository documentRepository;
    private final LicenseApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final RequestContext requestContext;
    private final Path uploadRoot;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            LicenseApplicationRepository applicationRepository,
            UserRepository userRepository,
            AuditLogService auditLogService,
            RequestContext requestContext,
            @Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.requestContext = requestContext;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public DocumentResponse upload(UUID applicationId, MultipartFile file, DocumentType type, UUID uploaderId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File exceeds the 5 MB limit (received %d bytes)".formatted(file.getSize()));
        }

        LicenseApplication app = findApplication(applicationId);
        User uploader = findUser(uploaderId);

        // Store file and compute SHA-256 in a single streaming pass
        String[] stored = storeAndChecksum(file, applicationId);
        String storagePath = stored[0];
        String checksum   = stored[1];

        // Determine next iteration and retire current versions
        int nextIteration = documentRepository.findByApplicationAndCurrentVersionTrue(app).stream()
                .mapToInt(Document::getIteration)
                .max()
                .orElse(0) + 1;

        if (nextIteration > 1) {
            documentRepository.markAllAsNotCurrentVersion(app);
        }

        Document saved = documentRepository.save(Document.builder()
                .application(app)
                .uploader(uploader)
                .documentType(type)
                .originalName(file.getOriginalFilename())
                .storagePath(storagePath)
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .sizeBytes(file.getSize())
                .checksumSha256(checksum)
                .iteration(nextIteration)
                .currentVersion(true)
                .build());

        auditLogService.log(AuditAction.DOCUMENT_UPLOADED, uploader, app,
                null, "{\"documentId\":\"%s\",\"type\":\"%s\"}".formatted(saved.getId(), type),
                requestContext.clientIp(), requestContext.userAgent(), AuditSeverity.INFO);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listCurrentDocuments(UUID applicationId) {
        LicenseApplication app = findApplication(applicationId);
        return documentRepository.findByApplicationAndCurrentVersionTrue(app).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getById(UUID applicationId, UUID documentId) {
        findApplication(applicationId);
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));
        if (!doc.getApplication().getId().equals(applicationId)) {
            throw new EntityNotFoundException("Document not found: " + documentId);
        }
        return toResponse(doc);
    }

    // Streams the file to disk and computes SHA-256 in one pass — avoids reading the stream twice.
    private String[] storeAndChecksum(MultipartFile file, UUID applicationId) {
        try {
            Path dir = uploadRoot.resolve(applicationId.toString());
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(file.getInputStream(), digest);
                 OutputStream out = Files.newOutputStream(target)) {
                in.transferTo(out);
            }

            return new String[]{
                    uploadRoot.relativize(target).toString(),
                    HexFormat.of().formatHex(digest.digest())
            };
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
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

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getApplication().getId(),
                d.getDocumentType(),
                d.getOriginalName(),
                d.getMimeType(),
                d.getSizeBytes(),
                d.getChecksumSha256(),
                d.getIteration(),
                d.isCurrentVersion(),
                d.getCreatedAt()
        );
    }
}
