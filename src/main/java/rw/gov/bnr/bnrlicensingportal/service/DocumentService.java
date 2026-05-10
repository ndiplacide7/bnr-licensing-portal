package rw.gov.bnr.bnrlicensingportal.service;

import org.springframework.web.multipart.MultipartFile;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.DocumentResponse;
import rw.gov.bnr.bnrlicensingportal.domain.enums.DocumentType;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse upload(UUID applicationId, MultipartFile file, DocumentType type, UUID uploaderId);
    List<DocumentResponse> listCurrentDocuments(UUID applicationId);
    DocumentResponse getById(UUID applicationId, UUID documentId);
}
