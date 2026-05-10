package rw.gov.bnr.bnrlicensingportal.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// Uses a real PostgreSQL container — not H2. H2 has different locking semantics
// and would give false-green results for concurrency tests.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApplicationFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void context_loads_with_real_postgres() {
        // Verifies the full application context starts against a real PostgreSQL container.
    }

    // TODO: happy-path flow — create DRAFT → submit → assign reviewer → complete review → approve
    // TODO: concurrent submission test to verify @Version optimistic locking throws OptimisticLockException
    // TODO: audit log immutability — verify no DELETE or UPDATE is possible on audit_logs
}
