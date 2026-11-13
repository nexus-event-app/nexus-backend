package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** La V18 junta cada viñeta con su texto en las ofertas y noticias ya guardadas. */
class V18ArreglarListasImportadasTest {

    @Test
    void fixesLonelyBulletsInJobsAndNews() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v18test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table job_offers (id bigint primary key, description clob not null)");
                sql.execute("create table news (id bigint primary key, content clob)");
                sql.execute("insert into job_offers values (1, 'Tareas:\n\n•\n\nTests.\n\n•\n\nEntrevistas.'), "
                        + "(2, 'Sin listas.')");
                sql.execute("insert into news values (1, 'Claves:\n\n•\n\nUna.\n\n•\n\nDos.'), (2, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V18__Arreglar_listas_importadas().migrate(context);

            assertThat(read(connection, "select description from job_offers where id = 1"))
                    .isEqualTo("Tareas:\n\n• Tests.\n• Entrevistas.");
            assertThat(read(connection, "select description from job_offers where id = 2")).isEqualTo("Sin listas.");
            assertThat(read(connection, "select content from news where id = 1")).isEqualTo("Claves:\n\n• Una.\n• Dos.");
        }
    }

    private static String read(Connection connection, String query) throws Exception {
        try (Statement sql = connection.createStatement(); ResultSet rows = sql.executeQuery(query)) {
            rows.next();
            return rows.getString(1);
        }
    }
}
