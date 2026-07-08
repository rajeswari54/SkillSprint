package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic smoke test for the SkillSprint application context.
 *
 * @TestPropertySource overrides DB and JWT properties so the full
 * ApplicationContext can load during CI/test without a real MySQL instance.
 *
 * H2 in-memory database is used in test scope only — no MySQL required.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Use H2 in-memory database instead of MySQL during tests
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // Valid 512-bit JWT secret for test context
        "jwt.secret=5A7234753778214125442A472D4B6150645367566B59703373367639792F423F4D6251655468576D5A7134743777217A25432A462D4A404E635266556A586E32",
        "jwt.expiration=86400000"
})
class DemoApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring ApplicationContext starts without errors.
        // If any bean wiring, configuration, or property issue exists, this test will catch it.
    }

}
