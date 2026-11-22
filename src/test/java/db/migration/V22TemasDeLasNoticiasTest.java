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

class V22TemasDeLasNoticiasTest {

    @Test
    void renamesTheOldCategoriesAndReclassifiesImportedNews() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v22test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table import_sources (id bigint primary key, name varchar(100), url varchar(500), "
                        + "news_category varchar(30), keywords varchar(500))");
                sql.execute("create table news (id bigint primary key, title varchar(200), content clob, "
                        + "category varchar(30) not null, source varchar(100), external_id varchar(500))");
                sql.execute("insert into import_sources values "
                        + "(1, 'Genbeta', 'https://www.genbeta.com/feedburner.xml', 'GENERAL', "
                        + "'programación, software, código, código abierto, GitHub'), "
                        + "(2, 'WeLiveSecurity en español', 'https://www.welivesecurity.com/es/rss/feed/', 'GENERAL', null), "
                        + "(3, 'Paradigma Digital', 'https://www.paradigmadigital.com/feed/', 'DESARROLLO', null)");
                sql.execute("insert into news values "
                        + "(1, 'OpenAI presenta un nuevo modelo de lenguaje', 'Resumen.', 'GENERAL', 'Genbeta', 'a'), "
                        + "(2, 'Consejos para tus redes sociales', 'Sin tema técnico.', 'GENERAL', 'WeLiveSecurity en español', 'b'), "
                        + "(3, 'Arquitectura hexagonal paso a paso', 'Patrones de diseño.', 'DESARROLLO', 'Paradigma Digital', 'c'), "
                        + "(4, 'Cómo preparar tu entrevista', 'Consejos.', 'ENTREVISTAS', null, null), "
                        + "(5, 'OpenAI presenta otro modelo de lenguaje', 'Texto.', 'NETWORKING', null, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V22__Temas_de_las_noticias().migrate(context);

            assertThat(column(connection, "select id, category from news")).containsExactlyInAnyOrderEntriesOf(Map.of(
                    1L, "IA", 2L, "CIBERSEGURIDAD", 3L, "PROGRAMACION", 4L, "CARRERA", 5L, "CARRERA"));
            assertThat(column(connection, "select id, news_category from import_sources"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of(1L, "GENERAL", 2L, "CIBERSEGURIDAD", 3L, "PROGRAMACION"));
            assertThat(column(connection, "select id, keywords from import_sources where id = 1"))
                    .containsEntry(1L, "programación, software, código fuente, código abierto, GitHub");
        }
    }

    private static Map<Long, String> column(Connection connection, String query) throws Exception {
        Map<Long, String> values = new HashMap<>();
        try (Statement sql = connection.createStatement(); ResultSet rows = sql.executeQuery(query)) {
            while (rows.next()) {
                values.put(rows.getLong(1), rows.getString(2));
            }
        }
        return values;
    }
}
