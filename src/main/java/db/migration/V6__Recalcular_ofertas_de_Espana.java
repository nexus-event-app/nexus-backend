package db.migration;

import com.nexus.backend.util.SpainLocation;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Vuelve a calcular {@code job_offers.in_spain} con {@link SpainLocation}, la misma lógica que usa la aplicación al
 * guardar. La V5 lo rellenó con una lista de ciudades más corta escrita en SQL; así queda una sola fuente de verdad.
 * También añade el índice para el orden público: España primero y después por fecha de publicación.
 */
public class V6__Recalcular_ofertas_de_Espana extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try (Statement ddl = connection.createStatement()) {
            ddl.execute("create index idx_jobs_spain_published on job_offers (in_spain, published_at)");
        }
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("select id, location, in_spain from job_offers");
             PreparedStatement update = connection.prepareStatement("update job_offers set in_spain = ? where id = ?")) {
            while (rows.next()) {
                boolean inSpain = SpainLocation.matches(rows.getString("location"));
                if (inSpain != rows.getBoolean("in_spain")) {
                    update.setBoolean(1, inSpain);
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
