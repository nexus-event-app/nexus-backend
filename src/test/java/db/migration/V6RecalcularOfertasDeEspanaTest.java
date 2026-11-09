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

/** La V6 corrige las ofertas que la V5 marcó con una lista de ciudades más corta. */
class V6RecalcularOfertasDeEspanaTest {

    @Test
    void recalculatesInSpainWithTheApplicationLogic() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:v6test;DB_CLOSE_DELAY=-1")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("create table job_offers (id bigint primary key, location varchar(120), "
                        + "in_spain boolean not null, published_at timestamp(6))");
                // Valencia y Sevilla quedaron sin marcar en la V5; «Remoto» estaba mal marcada a propósito.
                sql.execute("insert into job_offers values (1, 'Valencia', false, null), (2, 'Sevilla', false, null), "
                        + "(3, 'Santiago, Chile', false, null), (4, 'Remoto', true, null), (5, 'Madrid', true, null)");
            }
            Context context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V6__Recalcular_ofertas_de_Espana().migrate(context);

            Map<Long, Boolean> inSpain = new HashMap<>();
            try (Statement sql = connection.createStatement();
                 ResultSet rows = sql.executeQuery("select id, in_spain from job_offers")) {
                while (rows.next()) {
                    inSpain.put(rows.getLong(1), rows.getBoolean(2));
                }
            }
            assertThat(inSpain).containsExactlyInAnyOrderEntriesOf(Map.of(1L, true, 2L, true, 3L, false, 4L, false, 5L, true));
        }
    }
}
