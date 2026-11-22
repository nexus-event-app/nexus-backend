package db.migration;

import com.nexus.backend.util.SpanishCities;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class V26__Ciudad_limpia_en_los_eventos extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        try (Statement select = context.getConnection().createStatement();
             ResultSet rows = select.executeQuery("select id, city, address from community_events where city is not null");
             PreparedStatement update = context.getConnection().prepareStatement(
                     "update community_events set city = ?, address = ? where id = ?")) {
            while (rows.next()) {
                String city = rows.getString("city");
                if (!SpanishCities.looksLikeAddress(city)) {
                    continue;
                }
                Optional<String> found = SpanishCities.find(city);
                if (found.isPresent()) {
                    String address = rows.getString("address");
                    update.setString(1, found.get());
                    update.setString(2, address == null || address.isBlank() ? truncate(city) : address);
                    update.setLong(3, rows.getLong("id"));
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }

    private static String truncate(String text) {
        return text.length() > 200 ? text.substring(0, 200) : text;
    }
}
