package db.migration;

import com.nexus.backend.util.LanguageDetector;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V17__Ocultar_noticias_en_otros_idiomas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(
                     "select id, title, content from news where external_id is not null and is_active = true");
             PreparedStatement update = connection.prepareStatement("update news set is_active = false where id = ?")) {
            while (rows.next()) {
                if (LanguageDetector.isOtherLanguage(rows.getString("title") + ". " + rows.getString("content"))) {
                    update.setLong(1, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
