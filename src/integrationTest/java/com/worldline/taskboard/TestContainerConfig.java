package com.worldline.taskboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.utility.DockerImageName.*;

@Configuration
public class TestContainerConfig {
    @Bean
    //@ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(parse("postgres:16-alpine"))
                .withDatabaseName("taskboard-test")
                .withUsername("test")
                .withPassword("test")
                .withLabel("reuse", "true");
    }

}