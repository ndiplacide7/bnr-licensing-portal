package rw.gov.bnr.bnrlicensingportal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rw.gov.bnr.bnrlicensingportal.domain.entity.User;
import rw.gov.bnr.bnrlicensingportal.domain.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean accountNonLocked = user.getLockedUntil() == null
                || user.getLockedUntil().isBefore(OffsetDateTime.now());

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                user.isActive(),
                accountNonLocked
        );
    }
}
