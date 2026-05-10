package rw.gov.bnr.bnrlicensingportal.security;

import org.springframework.security.core.GrantedAuthority;
import rw.gov.bnr.bnrlicensingportal.domain.enums.Role;

import java.util.Collection;
import java.util.UUID;

public class AuthenticatedUser extends org.springframework.security.core.userdetails.User {

    private final UUID id;
    private final Role role;

    public AuthenticatedUser(UUID id, String email, String passwordHash, Role role,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled, boolean accountNonLocked) {
        super(email, passwordHash, enabled, true, true, accountNonLocked, authorities);
        this.id = id;
        this.role = role;
    }

    public UUID getId() { return id; }
    public Role getRole() { return role; }
}
