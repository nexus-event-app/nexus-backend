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

/** La V26 deja solo la ciudad en los eventos que llevaban la dirección en ella; lo demás no se toca. */
class V26CiudadLimpiaEnLosEventosTest {

    @Test
    void movesTheAddressOutOfTheCityWhenTheCityIsRecognised() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v26test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table community_events (id bigint primary key, city varchar(100), "
                        + "address varchar(200))");
                sql.execute("insert into community_events values "
                        + "(1, 'Doofinder C. de Cronos, 63 · Madrid', null), "
                        + "(2, 'Calle Pelai 12, Barcelona', 'Pelai 12 (3.ª planta)'), "
                        + "(3, 'Valencia', null), "
                        + "(4, 'Polígono 7, Villanueva de Nada', null), "
                        + "(5, null, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V26__Ciudad_limpia_en_los_eventos().migrate(context);

            Map<Long, String> cities = new HashMap<>();
            Map<Long, String> addresses = new HashMap<>();
            try (Statement sql = connection.createStatement();
                 ResultSet rows = sql.executeQuery("select id, city, address from community_events")) {
                while (rows.next()) {
                    cities.put(rows.getLong(1), rows.getString(2));
                    addresses.put(rows.getLong(1), rows.getString(3));
                }
            }
            assertThat(cities.get(1L)).isEqualTo("Madrid");
            assertThat(addresses.get(1L)).isEqualTo("Doofinder C. de Cronos, 63 · Madrid");
            // Si ya tenía dirección, se respeta.
            assertThat(cities.get(2L)).isEqualTo("Barcelona");
            assertThat(addresses.get(2L)).isEqualTo("Pelai 12 (3.ª planta)");
            assertThat(cities.get(3L)).isEqualTo("Valencia");
            // Sin una ciudad conocida, se queda como estaba (se corrige desde el panel).
            assertThat(cities.get(4L)).isEqualTo("Polígono 7, Villanueva de Nada");
            assertThat(cities.get(5L)).isNull();
        }
    }
}
