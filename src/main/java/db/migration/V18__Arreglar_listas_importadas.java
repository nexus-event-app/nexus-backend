package db.migration;

import com.nexus.backend.util.BulletText;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Recoloca las listas de las ofertas y noticias ya importadas: antes la viñeta quedaba sola en una línea y su texto
 * más abajo (ver {@link BulletText}). Las nuevas ya se importan bien.
 */
public class V18__Arreglar_listas_importadas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        fix(connection, "job_offers", "description");
        fix(connection, "news", "content");
    }

    private static void fix(Connection connection, String table, String column) throws SQLException {
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery(
                     "select id, " + column + " from " + table + " where " + column + " like '%•%'");
             PreparedStatement update = connection.prepareStatement(
                     "update " + table + " set " + column + " = ? where id = ?")) {
            while (rows.next()) {
                String current = rows.getString(column);
                String fixed = BulletText.fix(current);
                if (!fixed.equals(current)) {
                    update.setString(1, fixed);
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
