package com.nexus.backend.migration;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.entity.News;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.UserRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aplica las migraciones sobre un PostgreSQL real y comprueba que Hibernate valida el esquema.
 * Solo se ejecuta si se define NEXUS_TEST_POSTGRES_URL (y opcionalmente _USER y _PASSWORD), como en CI.
 * La base de datos indicada se vacía al empezar: usa una exclusiva para tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "NEXUS_TEST_POSTGRES_URL", matches = ".+")
class PostgresMigrationTest {

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("NEXUS_TEST_POSTGRES_URL"));
        registry.add("spring.datasource.username", () -> envOr("NEXUS_TEST_POSTGRES_USER", "nexus"));
        registry.add("spring.datasource.password", () -> envOr("NEXUS_TEST_POSTGRES_PASSWORD", "nexus"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    @TestConfiguration
    static class CleanDatabase {
        @Bean
        FlywayMigrationStrategy cleanThenMigrate() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    private Flyway flyway;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private ImportSourceRepository importSourceRepository;

    @Test
    void migrationsApplyAndMatchTheEntities() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().pending()).isEmpty();

        User user = new User();
        user.setName("Postgres");
        user.setEmail("postgres@test.com");
        user.setPassword("x");
        user.setRole(User.Role.ADMIN);
        assertThat(userRepository.save(user).getId()).isNotNull();

        News news = new News();
        news.setTitle("Noticia en PostgreSQL");
        news.setContent("Contenido");
        news.setCategory(NewsCategory.DESARROLLO);
        news.setExternalId("rss:test");
        newsRepository.save(news);
        assertThat(newsRepository.findExistingExternalIds(Set.of("rss:test", "rss:otro"))).containsExactly("rss:test");

        ImportSource source = new ImportSource();
        source.setName("Fuente");
        source.setType(ImportSourceType.GETONBRD_JOBS);
        source.setUrl("https://example.com");
        source.setIntervalMinutes(120);
        assertThat(importSourceRepository.save(source).getType()).isEqualTo(ImportSourceType.GETONBRD_JOBS);
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
