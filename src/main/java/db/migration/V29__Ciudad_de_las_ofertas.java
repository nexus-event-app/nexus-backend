package db.migration;

import com.nexus.backend.util.SpanishCities;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class V29__Ciudad_de_las_ofertas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try (Statement ddl = connection.createStatement()) {
            ddl.execute("alter table job_offers add column city varchar(80)");
            ddl.execute("create index idx_jobs_city on job_offers (city)");
        }
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("select id, location from job_offers");
             PreparedStatement update = connection.prepareStatement("update job_offers set city = ? where id = ?")) {
            while (rows.next()) {
                Optional<String> city = SpanishCities.canonical(rows.getString("location"));
                if (city.isPresent()) {
                    update.setString(1, city.get());
                    update.setLong(2, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }
}
