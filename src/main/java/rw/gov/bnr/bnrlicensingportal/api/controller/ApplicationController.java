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
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.DecideApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.WithdrawApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.ApplicationResponse;
import rw.gov.bnr.bnrlicensingportal.security.AuthenticatedUser;
import rw.gov.bnr.bnrlicensingportal.service.ApplicationService;

import java.util.UUID;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Applications")
@SecurityRequirement(name = "Bearer Authentication")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Create a new license application (DRAFT)")
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.create(request, principal.getId()));
    }

    @GetMapping
    @Operation(summary = "List applications filtered by the authenticated actor's role")
    public ResponseEntity<Page<ApplicationResponse>> list(Pageable pageable,
                                                           @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.listForActor(principal.getId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable UUID id,
                                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.getById(id, principal.getId()));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Submit application: DRAFT → SUBMITTED")
    public ResponseEntity<ApplicationResponse> submit(@PathVariable UUID id,
                                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.submit(id, principal.getId()));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('REVIEWER', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Assign reviewer: SUBMITTED → UNDER_REVIEW")
    public ResponseEntity<ApplicationResponse> assignReviewer(@PathVariable UUID id,
                                                               @RequestParam UUID reviewerId,
                                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.assignReviewer(id, reviewerId, principal.getId()));
    }

    @PatchMapping("/{id}/complete-review")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Complete review: UNDER_REVIEW → REVIEW_COMPLETED")
    public ResponseEntity<ApplicationResponse> completeReview(@PathVariable UUID id,
                                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.completeReview(id, principal.getId()));
    }

    @PatchMapping("/{id}/decide")
    @PreAuthorize("hasRole('APPROVER')")
    @Operation(summary = "Final decision: REVIEW_COMPLETED → APPROVED or REJECTED")
    public ResponseEntity<ApplicationResponse> decide(@PathVariable UUID id,
                                                       @Valid @RequestBody DecideApplicationRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.decide(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Withdraw application")
    public ResponseEntity<ApplicationResponse> withdraw(@PathVariable UUID id,
                                                         @Valid @RequestBody WithdrawApplicationRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(applicationService.withdraw(id, request.reason(), principal.getId()));
    }
}
