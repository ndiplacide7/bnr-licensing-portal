package rw.gov.bnr.bnrlicensingportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.DecideApplicationRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.ApplicationResponse;

import java.util.UUID;

@Service
public interface ApplicationService {
    ApplicationResponse create(CreateApplicationRequest request, UUID applicantId);
    ApplicationResponse getById(UUID id, UUID actorId);
    Page<ApplicationResponse> listForActor(UUID actorId, Pageable pageable);
    ApplicationResponse submit(UUID applicationId, UUID actorId);
    ApplicationResponse assignReviewer(UUID applicationId, UUID reviewerId, UUID actorId);
    ApplicationResponse completeReview(UUID applicationId, UUID actorId);
    ApplicationResponse decide(UUID applicationId, DecideApplicationRequest request, UUID actorId);
    ApplicationResponse withdraw(UUID applicationId, String reason, UUID actorId);
}
