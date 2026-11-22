package db.migration;

import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.importer.NewsClassifier;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class V22__Temas_de_las_noticias extends BaseJavaMigration {

    private static final Map<String, String> RENAMED = Map.of(
            "DESARROLLO", "PROGRAMACION",
            "RECURSOS", "PROGRAMACION",
            "ENTREVISTAS", "CARRERA",
            "NETWORKING", "CARRERA");

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        renameCategories(connection, "news", "category");
        renameCategories(connection, "import_sources", "news_category");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("update import_sources set news_category = 'CIBERSEGURIDAD' "
                    + "where url = 'https://www.welivesecurity.com/es/rss/feed/'");
            statement.executeUpdate("update import_sources set keywords = replace(keywords, "
                    + "'software, código, código abierto', 'software, código fuente, código abierto') "
                    + "where keywords like '%software, código, código abierto%'");
        }
        reclassifyImported(connection);
    }

    private static void renameCategories(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "update " + table + " set " + column + " = ? where " + column + " = ?")) {
            for (Map.Entry<String, String> entry : RENAMED.entrySet()) {
                update.setString(1, entry.getValue());
                update.setString(2, entry.getKey());
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private static void reclassifyImported(Connection connection) throws SQLException {
        Map<String, NewsCategory> sourceCategories = new HashMap<>();
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(
                     "select name, news_category from import_sources where news_category is not null")) {
            while (rows.next()) {
                sourceCategories.put(rows.getString("name"), NewsCategory.valueOf(rows.getString("news_category")));
            }
        }
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(
                     "select id, title, content, category, source from news where external_id is not null");
             PreparedStatement update = connection.prepareStatement("update news set category = ? where id = ?")) {
            while (rows.next()) {
                NewsCategory current = NewsCategory.valueOf(rows.getString("category"));
                NewsCategory fallback = sourceCategories.getOrDefault(rows.getString("source"), current);
                NewsCategory classified = NewsClassifier.classify(rows.getString("title"), rows.getString("content"),
                        fallback);
                if (classified != current) {
                    update.setString(1, classified.name());
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
