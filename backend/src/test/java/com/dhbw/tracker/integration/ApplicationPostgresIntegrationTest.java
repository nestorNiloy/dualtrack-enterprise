package com.dhbw.tracker.integration;

import com.dhbw.tracker.dto.ApplicationDtos.ApplicationRequest;
import com.dhbw.tracker.exception.ResourceNotFoundException;
import com.dhbw.tracker.model.*;
import com.dhbw.tracker.repository.*;
import com.dhbw.tracker.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ApplicationPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dualtrack_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private ApplicationService service;
    @Autowired private UserRepository users;
    @Autowired private ApplicationRepository applications;
    @Autowired private StatusHistoryRepository history;

    @BeforeEach
    void cleanDatabase() {
        history.deleteAll();
        applications.deleteAll();
        users.deleteAll();
    }

    @Test
    void postgresMigrationAndApplicationLifecycleWorkTogether() {
        User user = new User();
        user.setEmail("postgres-test@example.com");
        user.setPasswordHash("not-used-in-this-test");
        users.save(user);

        var created = service.create(user.getEmail(), new ApplicationRequest(
                "SAP", "Junior Java Developer", ApplicationStatus.APPLIED,
                LocalDate.of(2026, 11, 22), "Created in PostgreSQL integration test"));

        assertNotNull(created.id());
        assertEquals(1, applications.countByUserId(user.getId()));

        var updated = service.update(user.getEmail(), created.id(), new ApplicationRequest(
                "SAP", "Junior Java Developer", ApplicationStatus.INTERVIEWING,
                LocalDate.of(2026, 11, 22), "Interview scheduled"));

        assertEquals(ApplicationStatus.INTERVIEWING, updated.status());
        assertEquals(2, history.findAllByApplicationIdOrderByChangedAtDesc(created.id()).size());
        assertEquals(1, service.list(user.getEmail(), ApplicationStatus.INTERVIEWING, "", 0, 10)
                .totalElements());
        assertEquals(0, service.list(user.getEmail(), ApplicationStatus.APPLIED, "", 0, 10)
                .totalElements());
    }

    @Test
    void userCannotReadOrModifyAnotherUsersApplication() {
        User first = user("first@example.com");
        User second = user("second@example.com");

        var created = service.create(first.getEmail(), new ApplicationRequest(
                "Acme", "Developer", ApplicationStatus.APPLIED, LocalDate.now(), null));

        assertThrows(ResourceNotFoundException.class, () -> service.update(second.getEmail(),
                created.id(), new ApplicationRequest("Acme", "Developer",
                        ApplicationStatus.REJECTED, LocalDate.now(), null)));
        assertThrows(ResourceNotFoundException.class, () ->
                service.history(second.getEmail(), created.id()));
        assertEquals(1, service.list(first.getEmail(), null, "", 0, 10).totalElements());
        assertEquals(0, service.list(second.getEmail(), null, "", 0, 10).totalElements());
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("not-used-in-this-test");
        return users.save(user);
    }
}
