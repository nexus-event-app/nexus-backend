package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** La V7 lleva las fuentes recomendadas actuales a bases de datos que ya existían, sin pisar lo personalizado. */
class V7FuentesRecomendadasTest {

    private static final String OLD_GENBETA = "programación, programador, programadora, desarrollador, desarrolladora, "
            + "desarrollo, software, código, open source, código abierto, GitHub, Python, JavaScript, Java, Linux, "
            + "inteligencia artificial, IA, ChatGPT, developer, ciberseguridad, empleo, Microsoft, Google, API, nube";

    @Test
    void updatesExistingDatabasesAndRespectsCustomFilters() throws Exception {
        String url = "jdbc:h2:mem:v7existing;DB_CLOSE_DELAY=-1";
        migrate(url, "6");
        try (Connection connection = DriverManager.getConnection(url, "sa", ""); Statement sql = connection.createStatement()) {
            insert(sql, "Genbeta", "RSS_NEWS", "https://www.genbeta.com/feedburner.xml", true, OLD_GENBETA);
            insert(sql, "Xataka", "RSS_NEWS", "https://www.xataka.com/feedburner.xml", true, "mi filtro propio");
            insert(sql, "DEV Community · #beginners", "RSS_NEWS", "https://dev.to/feed/tag/beginners", true, null);
        }

        migrate(url, "7");

        try (Connection connection = DriverManager.getConnection(url, "sa", ""); Statement sql = connection.createStatement()) {
            Map<String, String[]> rows = read(sql);
            assertThat(rows.get("Genbeta")[0]).startsWith("programación, programador, desarrollador").doesNotContain("ChatGPT");
            assertThat(rows.get("Genbeta")[1]).contains("contenido para adultos", "apuestas");
            assertThat(rows.get("Xataka")[0]).isEqualTo("mi filtro propio");
            assertThat(rows.get("Xataka")[1]).isNull();
            assertThat(rows.get("DEV Community · #beginners")[2]).isEqualToIgnoringCase("false");
            assertThat(rows).containsKey("Jobicy · Europa");
            assertThat(rows.get("Jobicy · Europa")[3]).isEqualTo("JOBICY_JOBS");
        }
    }

    @Test
    void doesNothingOnAnEmptyDatabase() throws Exception {
        String url = "jdbc:h2:mem:v7empty;DB_CLOSE_DELAY=-1";
        migrate(url, "7");
        try (Connection connection = DriverManager.getConnection(url, "sa", ""); Statement sql = connection.createStatement();
             ResultSet count = sql.executeQuery("select count(*) from import_sources")) {
            count.next();
            assertThat(count.getInt(1)).as("las fuentes las crea la aplicación al arrancar").isZero();
        }
    }

    private static void migrate(String url, String target) {
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").target(target).load().migrate();
    }

    private static void insert(Statement sql, String name, String type, String url, boolean active, String keywords)
            throws Exception {
        sql.execute("insert into import_sources (name, type, url, auto_publish, active, remote_only, keywords, "
                + "interval_minutes, created_at) values ('" + name + "', '" + type + "', '" + url + "', true, " + active
                + ", false, " + (keywords == null ? "null" : "'" + keywords + "'") + ", 180, current_timestamp)");
    }

    private static Map<String, String[]> read(Statement sql) throws Exception {
        Map<String, String[]> rows = new HashMap<>();
        try (ResultSet rs = sql.executeQuery("select name, keywords, exclude_keywords, active, type from import_sources")) {
            while (rs.next()) {
                rows.put(rs.getString(1), new String[] {rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        }
        return rows;
    }
}
