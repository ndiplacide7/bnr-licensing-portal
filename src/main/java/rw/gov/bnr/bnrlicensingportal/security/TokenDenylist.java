package rw.gov.bnr.bnrlicensingportal.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dev: In-memory token denylist for stateless logout.
 * Prod: I'll use Redis
 */
@Component
public class TokenDenylist {

    private final ConcurrentHashMap<String, Instant> deniedTokens = new ConcurrentHashMap<>();

    public void deny(String token, Instant expiresAt) {
        deniedTokens.put(token, expiresAt);
    }

    public boolean isDenied(String token) {
        return deniedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3_600_000)
    public void evictExpired() {
        Instant now = Instant.now();
        deniedTokens.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}
