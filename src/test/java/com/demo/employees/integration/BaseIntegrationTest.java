package com.demo.employees.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base class for integration tests providing a shared Testcontainers MySQL instance
 * and authenticated {@link TestRestTemplate} helpers for admin and user roles.
 * Uses the singleton container pattern so all test classes share one MySQL container.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /** Shared MySQL container — started once, reused across all integration test classes. */
    static final MySQLContainer<?> mysql;

    static {
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("employees_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * Returns a {@link TestRestTemplate} authenticated with ADMIN credentials.
     *
     * @return an admin-authenticated rest template
     */
    protected TestRestTemplate adminTemplate() {
        return restTemplate.withBasicAuth("admin", "admin123");
    }

    /**
     * Returns a {@link TestRestTemplate} authenticated with USER credentials.
     *
     * @return a user-authenticated rest template
     */
    protected TestRestTemplate userTemplate() {
        return restTemplate.withBasicAuth("user", "user123");
    }
}
