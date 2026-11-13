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

/** La V19 corrige las noticias importadas que «Publicar» había pasado a castellano. */
class V19CorregirIdiomaDeNoticiasPublicadasTest {

    @Test
    void redetectsImportedNewsAndFallsBackToTheSourceLanguage() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v19test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table import_sources (id bigint primary key, name varchar(100), language varchar(5))");
                sql.execute("create table news (id bigint primary key, title varchar(200), content clob, "
                        + "language varchar(5) not null, external_id varchar(500), source varchar(100))");
                sql.execute("insert into import_sources values (1, 'DEV Community', 'en'), (2, 'Genbeta', 'es')");
                sql.execute("insert into news values "
                        // Publicada desde el panel: en inglés pero guardada como castellano.
                        + "(1, 'PowerShell -like vs -match: the real difference', 'You wanted only the log files and "
                        + "you wrote a filter that returned too many results.', 'es', 'a', 'DEV Community'), "
                        // El texto no deja claro el idioma: se usa el de su fuente.
                        + "(2, 'Kubernetes 1.31', 'Kubernetes 1.31', 'es', 'b', 'DEV Community'), "
                        + "(3, 'Cómo preparar tu primera entrevista', 'Consejos para quien busca su primer empleo.', "
                        + "'es', 'c', 'Genbeta'), "
                        // Publicada a mano: no se toca.
                        + "(4, 'What is Docker and why should you learn it?', 'A guide.', 'es', null, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V19__Corregir_idioma_de_noticias_publicadas().migrate(context);

            Map<Long, String> languages = new HashMap<>();
            try (Statement sql = connection.createStatement();
                 ResultSet rows = sql.executeQuery("select id, language from news")) {
                while (rows.next()) {
                    languages.put(rows.getLong(1), rows.getString(2));
                }
            }
            assertThat(languages).containsExactlyInAnyOrderEntriesOf(Map.of(1L, "en", 2L, "en", 3L, "es", 4L, "es"));
        }
    }
}
