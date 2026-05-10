package rw.gov.bnr.bnrlicensingportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateUserRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;

import java.util.UUID;


public interface UserService {
    UserResponse createUser(CreateUserRequest request, UUID createdById);
    UserResponse getUserById(UUID id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deactivateUser(UUID id, UUID actorId);
}
