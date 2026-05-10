package rw.gov.bnr.bnrlicensingportal.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateUserRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.AuditLogResponse;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;
import rw.gov.bnr.bnrlicensingportal.security.AuthenticatedUser;
import rw.gov.bnr.bnrlicensingportal.service.AuditLogService;
import rw.gov.bnr.bnrlicensingportal.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "List all users")
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create a new user account")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request, principal.getId()));
    }

    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Deactivate a user account — users are never deleted")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id,
                                                @AuthenticationPrincipal AuthenticatedUser principal) {
        userService.deactivateUser(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "View the full audit trail")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }

    @GetMapping("/audit-logs/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "View audit trail for a specific application")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsForApplication(
            @PathVariable UUID applicationId, Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsForApplication(applicationId, pageable));
    }
}
