package rw.gov.bnr.bnrlicensingportal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.bnr.bnrlicensingportal.api.dto.request.CreateUserRequest;
import rw.gov.bnr.bnrlicensingportal.api.dto.response.UserResponse;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditSeverity;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;
import rw.gov.bnr.bnrlicensingportal.security.RequestContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final RequestContext requestContext;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID createdById) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("A user with email " + request.email() + " already exists");
        }
        User creator = findUser(createdById);
        User saved = userRepository.save(User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .passwordHash(passwordEncoder.encode(request.temporaryPassword()))
                .createdBy(creator)
                .build());

        auditLogService.log(AuditAction.USER_CREATED, creator,
                requestContext.clientIp(), requestContext.userAgent());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return toResponse(findUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id, UUID actorId) {
        User user = findUser(id);
        if (!user.isActive()) {
            throw new IllegalStateException("User " + id + " is already deactivated");
        }
        User actor = findUser(actorId);
        user.setActive(false);
        userRepository.save(user);

        auditLogService.log(AuditAction.USER_DEACTIVATED, actor,
                requestContext.clientIp(), requestContext.userAgent());
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getRole(), u.isActive(), u.getLastLoginAt(), u.getCreatedAt());
    }
}
