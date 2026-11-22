package db.migration;

import com.nexus.backend.util.LanguageDetector;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V16__Detectar_idioma_de_las_noticias extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(
                     "select id, title, content, language from news where external_id is not null");
             PreparedStatement update = connection.prepareStatement("update news set language = ? where id = ?")) {
            while (rows.next()) {
                String current = rows.getString("language");
                String detected = LanguageDetector.detectOr(rows.getString("title") + ". " + rows.getString("content"),
                        current);
                if (!detected.equals(current)) {
                    update.setString(1, detected);
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
