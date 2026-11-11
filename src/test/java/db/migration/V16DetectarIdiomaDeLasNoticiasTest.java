package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** La V16 corrige el idioma de las noticias importadas que la V14 dejó en castellano por venir de otra fuente. */
class V16DetectarIdiomaDeLasNoticiasTest {

    @Test
    void redetectsTheLanguageOfImportedNewsOnly() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v16test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table news (id bigint primary key, title varchar(200), content clob, "
                        + "language varchar(5) not null, external_id varchar(500))");
                sql.execute("insert into news values "
                        + "(1, 'What is Docker and why should you learn it?', 'A guide for beginners.', 'es', 'a'), "
                        + "(2, 'Qué es Docker y por qué aprenderlo', 'Una guía para empezar.', 'en', 'b'), "
                        + "(3, 'Kubernetes 1.31', 'Kubernetes 1.31', 'en', 'c'), "
                        // Publicada a mano: se respeta el idioma que eligió quien la publicó.
                        + "(4, 'What is Docker and why should you learn it?', 'A guide.', 'es', null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V16__Detectar_idioma_de_las_noticias().migrate(context);

            Map<Long, String> languages = new HashMap<>();
            try (Statement sql = connection.createStatement();
                 ResultSet rows = sql.executeQuery("select id, language from news")) {
                while (rows.next()) {
                    languages.put(rows.getLong(1), rows.getString(2));
                }
            }
            assertThat(languages).containsExactlyInAnyOrderEntriesOf(Map.of(1L, "en", 2L, "es", 3L, "en", 4L, "es"));
        }
    }
}
