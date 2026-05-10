package rw.gov.bnr.bnrlicensingportal.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.DocumentResponse;
import rw.gov.bnr.bnrlicensingportal.domain.enums.DocumentType;
import rw.gov.bnr.bnrlicensingportal.security.AuthenticatedUser;
import rw.gov.bnr.bnrlicensingportal.service.DocumentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/documents")
@RequiredArgsConstructor
@Tag(name = "Documents")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Upload a document for an application")
    public ResponseEntity<DocumentResponse> upload(@PathVariable UUID applicationId,
                                                    @RequestPart MultipartFile file,
                                                    @RequestParam DocumentType documentType,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(applicationId, file, documentType, principal.getId()));
    }

    @GetMapping
    @Operation(summary = "List current documents for an application")
    public ResponseEntity<List<DocumentResponse>> list(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(documentService.listCurrentDocuments(applicationId));
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get a specific document")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID applicationId,
                                                     @PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getById(applicationId, documentId));
    }
}
