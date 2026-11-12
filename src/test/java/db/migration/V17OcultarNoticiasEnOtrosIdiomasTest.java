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

/** La V17 despublica las noticias importadas que no están ni en castellano ni en inglés. */
class V17OcultarNoticiasEnOtrosIdiomasTest {

    @Test
    void hidesImportedNewsInOtherLanguagesOnly() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v17test;DB_CLOSE_DELAY=-1")) {
            String danish = "'Byg en enkel trælog med JavaScript og CSV', 'Et gammelt træ i haven kan ændre sig over tid. "
                    + "Efter en storm opdager man noget, der ikke var der sidste år.'";
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table news (id bigint primary key, title varchar(200), content clob, "
                        + "is_active boolean, external_id varchar(500))");
                sql.execute("insert into news values "
                        + "(1, " + danish + ", true, 'a'), "
                        + "(2, 'What is Docker and why should you learn it?', 'A guide for beginners.', true, 'b'), "
                        + "(3, 'Qué es Docker y por qué aprenderlo', 'Una guía para empezar.', true, 'c'), "
                        // Publicada a mano: no se toca.
                        + "(4, " + danish + ", true, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V17__Ocultar_noticias_en_otros_idiomas().migrate(context);

            Map<Long, Boolean> active = new HashMap<>();
            try (Statement sql = connection.createStatement();
                 ResultSet rows = sql.executeQuery("select id, is_active from news")) {
                while (rows.next()) {
                    active.put(rows.getLong(1), rows.getBoolean(2));
                }
            }
            assertThat(active).containsExactlyInAnyOrderEntriesOf(Map.of(1L, false, 2L, true, 3L, true, 4L, true));
        }
    }
}
