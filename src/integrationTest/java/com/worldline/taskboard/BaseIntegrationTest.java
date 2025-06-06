package com.worldline.taskboard;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskboard-test")
            .withUsername("test")
            .withPassword("test")
            .withLabel("reuse", "true");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/db.changelog-test.xml");
        registry.add("spring.liquibase.duplicateFileMode", () -> "WARN");
        registry.add("spring.security.enabled", () -> "false");
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    protected String getBasePath() {
        return "/api/taskboard";
    }

    protected String getBaseUri() {
        return "http://localhost";
    }

}
